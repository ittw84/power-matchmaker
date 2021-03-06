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
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

/**
 * This will correct the street type code to either be before or after
 * the street name.
 */
public class StreetTypePrefixStep implements ValidateStep {

    /**
     * The address database that contains all of the street types.
     */
    private final AddressDatabase db;

    public StreetTypePrefixStep(AddressDatabase db) {
        this.db = db;
    }

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (a.isStreetTypePrefix() != ValidateStepUtil.isStreetTypePrefix(db, suggestion, pc)) {
            state.addError(ValidateResult.createValidateResult(
                    Status.FAIL, "Street type prefix does not agree with postal code"));
            suggestion.setStreetTypePrefix(ValidateStepUtil.isStreetTypePrefix(db, suggestion, pc));
            if (state.isCountErrors()) {
                state.setSuggestionExists(true);
            }
        }
        return false;
    }

}
