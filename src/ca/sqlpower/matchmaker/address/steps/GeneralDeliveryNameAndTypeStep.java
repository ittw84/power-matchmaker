/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.address.steps;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.PostalCode;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.ForwardCursor;

/**
 * This will correct the name and type of the delivery installation if it is different
 * or missing from the postal code.
 */
public class GeneralDeliveryNameAndTypeStep implements ValidateStep {
    
    private final AddressDatabase db;

    public GeneralDeliveryNameAndTypeStep(AddressDatabase db) {
        this.db = db;
    }

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) throws DatabaseException {
        if (a.getDeliveryInstallationName() != null || a.getDeliveryInstallationType() != null) {
            ValidateStepUtil.correctDeliveryInstallation(a, pc, suggestion, state);
        } else {
            EntityJoin<Long, PostalCode> join = new EntityJoin<Long, PostalCode>(db.getPostalCodePK());
            if (a.getProvince() != null) {
                join.addCondition(db.getPostalCodeProvince(), a.getProvince());
            }
            if (a.getMunicipality() != null) {
                join.addCondition(db.getPostalCodeMunicipality(), a.getMunicipality());
            }
            if (a.getType() != null) {
                join.addCondition(db.getPostalCodeRecordType(), a.getType().getRecordTypeCode());
            }
            ForwardCursor<PostalCode> matches = null;
            try {
                matches = join.entities();
                for (PostalCode similarPC : matches) {
                    if (similarPC != pc) {
                        ValidateStepUtil.correctDeliveryInstallation(a, pc, suggestion, state);
                    }
                }
            } finally {
                if (matches != null) matches.close();
            }
        }
        return false;
    }

}
