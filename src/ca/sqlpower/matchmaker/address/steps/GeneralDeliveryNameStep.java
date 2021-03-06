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

/**
 * Checks if the general delivery name is an exact match to one of the valid
 * general delivery names. If it is not the suggestion will be updated.
 */
public class GeneralDeliveryNameStep implements ValidateStep {

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (!Address.isGeneralDeliveryExactMatch(a.getGeneralDeliveryName())) {
            if ((Address.isGeneralDelivery(a.getGeneralDeliveryName()) || a.getGeneralDeliveryName() == null) && !a.getProvince().equals(AddressDatabase.QUEBEC_PROVINCE_CODE)
                    && ValidateStepUtil.different(a.getGeneralDeliveryName(), Address.GENERAL_DELIVERY_ENGLISH)) {
                suggestion.setGeneralDeliveryName(Address.GENERAL_DELIVERY_ENGLISH);
                state.incrementErrorCount("English general delivery name is incorrectly spelled and/or abbreviated.");
            } else if ((Address.isGeneralDelivery(a.getGeneralDeliveryName()) || a.getGeneralDeliveryName() == null) && a.getProvince().equals(AddressDatabase.QUEBEC_PROVINCE_CODE)
                    && ValidateStepUtil.different(a.getGeneralDeliveryName(), Address.GENERAL_DELIVERY_FRENCH)) {
                suggestion.setGeneralDeliveryName(Address.GENERAL_DELIVERY_FRENCH);
                state.incrementErrorCount("French general delivery name is incorrectly spelled and/or abbreviated.");
            }
        }
        return false;
    }

}
