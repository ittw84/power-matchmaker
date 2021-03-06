/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.MatchMakerEngine.EngineMode;
import ca.sqlpower.matchmaker.MungeSettings.AutoValidateSetting;
import ca.sqlpower.matchmaker.MungeSettings.PoolFilterSetting;
import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressPool;
import ca.sqlpower.matchmaker.address.AddressResult;
import ca.sqlpower.matchmaker.address.AddressValidator;
import ca.sqlpower.matchmaker.address.AddressCorrectionEngine.AddressCorrectionEngineMode;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonBound;
import ca.sqlpower.object.annotation.Transient;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLIndex.Column;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

/**
 * An MungeStep that takes in a supposed mailing address as inputs and then
 * tries to parse, validate, and correct it based on a database derived from the
 * Canadian postal database. Note that this MungeStep currently only supports
 * Canadian mailing addresses, and mailing addresses from other countries cannot
 * be expected to be parsed, validated, or corrected properly.
 */
public class AddressCorrectionMungeStep extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	private Logger logger = Logger.getLogger(AddressCorrectionMungeStep.class);
	
	private String addressCorrectionDataPath;
	
	private AddressDatabase addressDB;
	
	private boolean addressCorrected;
	
	private MungeStep inputStep;
	
	private AddressPool pool;
	
	public enum AddressStatus {
		/**
		 * Address is SERP valid
		 */
		VALID,
		/**
		 * Address is SERP correctable
		 */
		CORRECTABLE,
		/**
		 * Address is cannot be corrected with SERP
		 */
		INCORRECTABLE
	}
	
	private AddressStatus addressStatus;
	
	@Constructor
	public AddressCorrectionMungeStep() {
		super("Address Correction", false);
		
	}

	public void init() {

		addChild(new MungeStepOutput<String>("Address Line 1", String.class));
		addChild(new MungeStepOutput<String>("Address Line 2", String.class));
		addChild(new MungeStepOutput<String>("Suite", String.class));
		addChild(new MungeStepOutput<BigDecimal>("Street Number", BigDecimal.class));
		addChild(new MungeStepOutput<String>("Street Number Suffix", String.class));
		addChild(new MungeStepOutput<String>("Street", String.class));
		addChild(new MungeStepOutput<String>("Street Type", String.class));
		addChild(new MungeStepOutput<String>("Street Direction", String.class));
		addChild(new MungeStepOutput<String>("Municipality", String.class));
		addChild(new MungeStepOutput<String>("Province", String.class));
		addChild(new MungeStepOutput<String>("Country", String.class));
		addChild(new MungeStepOutput<String>("Postal/ZIP", String.class));
		// A new output requested. Basically, it will return either the value of
		// the validator's isSERPValid if using a validator, or just return the
		// address's isValid flag in the case of just writing values from the
		// result table
		addChild(new MungeStepOutput<Boolean>("Is Valid?", Boolean.class));
		
		InputDescriptor input0 = new InputDescriptor("Address Line 1", String.class);
		InputDescriptor input1 = new InputDescriptor("Address Line 2", String.class);
		InputDescriptor input2 = new InputDescriptor("Municipality", String.class);
		InputDescriptor input3 = new InputDescriptor("Province", String.class);
		InputDescriptor input4 = new InputDescriptor("Country", String.class);
		InputDescriptor input5 = new InputDescriptor("Postal/ZIP", String.class);
		
		super.addInput(input0);
		super.addInput(input1);
		super.addInput(input2);
		super.addInput(input3);
		super.addInput(input4);
		super.addInput(input5);
		
	}
	
	@NonBound
	public void setInputStep(MungeStep inputStep) {
		this.inputStep = inputStep;
	}
	
	@Override
	public void doOpen(EngineMode mode, Logger logger) throws Exception {
		this.logger = logger;
		
		if (mode instanceof AddressCorrectionEngineMode) {
			this.mode = (AddressCorrectionEngineMode) mode;
		} else if (mode != null) {
			throw new IllegalArgumentException("Address Correction Step only accepts StepModes of type AddressCorrectionMungeStep.AddressCorrectionMode");
		}
		validateDatabase();
		
	}
	
	@Override
	public void refresh(Logger logger) throws Exception {
		validateDatabase();
	}

	/**
	 * This will get the address database's path from the context and
	 * try to connect to it. If the database cannot be connected to the
	 * database will be null.
	 */
	public void validateDatabase() {
		MatchMakerSession session = getSession();
		MatchMakerSessionContext context = session.getContext();
		setAddressCorrectionDataPath(context.getAddressCorrectionDataPath());
		
		
		String addressCorrectionDataPath = getAddressCorrectionDataPath();
		try {
			setAddressDB(new AddressDatabase(new File(addressCorrectionDataPath)));
		} catch (Exception e) {
			setAddressDB(null);
		}
	}
	
	@Override
	public Boolean doCall() throws Exception {
		if (addressDB == null) {
			return false;
		}
		if (mode == AddressCorrectionEngineMode.ADDRESS_CORRECTION_WRITE_BACK_ADDRESSES) {
			return doCallWriteBackCorrectedAddresses();
		} else if (mode == AddressCorrectionEngineMode.ADDRESS_CORRECTION_PARSE_AND_CORRECT_ADDRESSES) {
			return doCallParseAndCorrect();
		} else if (mode == null) {
			return doCallNormalize();
		} else {
			throw new IllegalStateException("Address Correction Step does not support this mode: " + mode);
		}
	}
	
	/**
	 * Normalize addresses for deduping
	 * @return
	 */
	private Boolean doCallNormalize() throws Exception {
		MungeStepOutput addressLine1MSO = getMSOInputs().get(0);
		String addressLine1 = (addressLine1MSO != null) ? (String)addressLine1MSO.getData(): null;
		MungeStepOutput addressLine2MSO = getMSOInputs().get(1);
		String addressLine2 = (addressLine2MSO != null) ? (String)addressLine2MSO.getData() : null;
		MungeStepOutput municipalityMSO = getMSOInputs().get(2);
		String municipality = (municipalityMSO != null) ? (String)municipalityMSO.getData() : null;
		MungeStepOutput provinceMSO = getMSOInputs().get(3);
		String province = (provinceMSO != null) ? (String)provinceMSO.getData() : null;
		MungeStepOutput countryMSO = getMSOInputs().get(4);
		String country = (countryMSO != null) ? (String)countryMSO.getData() : null;
		MungeStepOutput postalCodeMSO = getMSOInputs().get(5);
		String inPostalCode = (postalCodeMSO != null) ? (String)postalCodeMSO.getData() : null;
		
		// nicely formatted 
		String addressString = addressLine1 + ", " + addressLine2 + ", " + municipality + ", " + province + ", " + inPostalCode + ", " + country;
		logger.debug("Parsing Address: " + addressString);
		Address address = Address.parse(addressLine1, municipality, province, inPostalCode, country, addressDB);
		
		logger.debug("Address that was parsed:\n" + address.toString());
		
		AddressValidator validator = new AddressValidator(addressDB, address);
		validator.validate();
		
		Address output;
		
		if (validator.getSuggestions().size() != 0 && validator.isValidSuggestion()) {
			output = validator.getSuggestions().get(0);
			logger.debug("Normalizing address to " + output);
		} else {
			output = address;
		}
		
		List<MungeStepOutput> outputs = getChildren(MungeStepOutput.class); 
		outputs.get(0).setData(output.getAddress());
		outputs.get(1).setData(addressLine2);
		outputs.get(2).setData(output.getSuite());
		outputs.get(3).setData(output.getStreetNumber() != null ? BigDecimal.valueOf(output.getStreetNumber()) : null);
		outputs.get(4).setData(output.getStreetNumberSuffix());
		outputs.get(5).setData(output.getStreet());
		outputs.get(6).setData(output.getStreetType());
		outputs.get(7).setData(output.getStreetDirection());
		outputs.get(8).setData(output.getMunicipality());
		outputs.get(9).setData(output.getProvince());
		outputs.get(10).setData(country);
		outputs.get(11).setData(output.getPostalCode());
		outputs.get(12).setData(validator.isSerpValid());
		
		return true;
	}

	/**
	 * Uses the user validated values stored in the from the result table where
	 * available. Otherwise, if no value is available, it will default to the
	 * auto-corrected value.
	 * 
	 * @return
	 * @throws Exception
	 *             Any Exceptions will get passed along to the SPSwingWorker
	 *             running this process.
	 */
	private Boolean doCallWriteBackCorrectedAddresses() throws Exception {
		logger.debug("Running with user validated addresses as output");
		
		SQLIndex uniqueKey = getProject().getSourceTableIndex();

		List<Object> uniqueKeyValues = new ArrayList<Object>();

		for (Column col: uniqueKey.getChildren(Column.class)) {
			MungeStepOutput output = inputStep.getOutputByName(col.getName());
			if (output == null) {
				throw new IllegalStateException("Input step is missing unique key column '" + col.getName() + "'");
			}
			uniqueKeyValues.add(output.getData());
		}
		
		AddressResult result = pool.findAddress(uniqueKeyValues);
		
		if (result != null && 
				result.getOutputAddress() != null &&
				/*!result.getOutputAddress().isEmptyAddress() && */ 
				result.isValid()) {
			Address address = result.getOutputAddress();
			
			MungeStepOutput addressLine2MSO = getMSOInputs().get(1);
			String addressLine2 = (addressLine2MSO != null) ? (String)addressLine2MSO.getData() : null;
			MungeStepOutput countryMSO = getMSOInputs().get(4);
			String country = (countryMSO != null) ? (String)countryMSO.getData() : null;
			
			logger.debug("Found an output address:\n" + address);
			List<MungeStepOutput> outputs = getChildren(MungeStepOutput.class); 
			outputs.get(0).setData(address.getAddress());
			outputs.get(1).setData(addressLine2);
			outputs.get(2).setData(address.getSuite());
			outputs.get(3).setData(address.getStreetNumber() != null ? BigDecimal.valueOf(address.getStreetNumber()) : null);
			outputs.get(4).setData(address.getStreetNumberSuffix());
			outputs.get(5).setData(address.getStreet());
			outputs.get(6).setData(address.getStreetType());
			outputs.get(7).setData(address.getStreetDirection());
			outputs.get(8).setData(address.getMunicipality());
			outputs.get(9).setData(address.getProvince());
			outputs.get(10).setData(country);
			outputs.get(11).setData(address.getPostalCode());
			outputs.get(12).setData(result.isValid());
			addressCorrected = true;
			pool.markAddressForDeletion(uniqueKeyValues);
		} else {
			addressCorrected = false;
		}

		return Boolean.TRUE;
	}
	
	private Boolean isAddressEqualToSuggested(Address address, Address suggestedAddress) {
		if (StringUtils.equals(address.getAddress(),      suggestedAddress.getAddress()) &&
			StringUtils.equals(address.getMunicipality(), suggestedAddress.getMunicipality()) &&
			StringUtils.equals(address.getProvince(),     suggestedAddress.getProvince())) {
			logger.debug("Suggested address is exactly the same, so skipping");
			logger.debug("Only one suggestion and it's the same, so skipping");
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	private Boolean doCallParseAndCorrect() throws Exception{
		addressCorrected = false;
		
		MungeStepOutput addressLine1MSO = getMSOInputs().get(0);
		String addressLine1 = (addressLine1MSO != null) ? (String)addressLine1MSO.getData(): null;
		MungeStepOutput addressLine2MSO = getMSOInputs().get(1);
		String addressLine2 = (addressLine2MSO != null) ? (String)addressLine2MSO.getData() : null;
		MungeStepOutput municipalityMSO = getMSOInputs().get(2);
		String municipality = (municipalityMSO != null) ? (String)municipalityMSO.getData() : null;
		MungeStepOutput provinceMSO = getMSOInputs().get(3);
		String province = (provinceMSO != null) ? (String)provinceMSO.getData() : null;
		MungeStepOutput countryMSO = getMSOInputs().get(4);
		String country = (countryMSO != null) ? (String)countryMSO.getData() : null;
		MungeStepOutput postalCodeMSO = getMSOInputs().get(5);
		String inPostalCode = (postalCodeMSO != null) ? (String)postalCodeMSO.getData() : null;
		
		// nicely formatted 
		String addressString = addressLine1 + ", " + addressLine2 + ", " + municipality + ", " + province + ", " + inPostalCode + ", " + country;
		logger.debug("Parsing Address: " + addressString);
		Address address = Address.parse(addressLine1, municipality, province, inPostalCode, country, addressDB);
		
		logger.debug("Address that was parsed:\n" + address.toString());
		
		AddressValidator validator = new AddressValidator(addressDB, address);
		validator.validate();
		
		if (validator.isSerpValid()) {
			addressStatus = AddressStatus.VALID;
		} else if (validator.isValidSuggestion()) {
			addressStatus = AddressStatus.CORRECTABLE;
		} else {
			addressStatus = AddressStatus.INCORRECTABLE;
		}
		
		PoolFilterSetting setting = getProject().getMungeSettings().getPoolFilterSetting();
		
		if (setting == PoolFilterSetting.NOTHING) {
			return Boolean.TRUE;
		} else if (setting == PoolFilterSetting.INVALID_ONLY) {
			if (validator.isSerpValid()) {
				logger.debug("This address is SERP valid, so skipping");
				return Boolean.TRUE;
			}
		} else if (setting == PoolFilterSetting.DIFFERENT_FORMAT_ONLY) {
			List<Address> suggestions = validator.getSuggestions();
			if (suggestions.size() == 0) {
				logger.debug("No suggestions, so skipping");
				return Boolean.TRUE;
			} else if (!validator.isSerpValid()) {
				logger.debug("Invalid address, so skipping");
				return Boolean.TRUE;
			} else if (suggestions.size() == 1) {
				Address suggestedAddress = suggestions.get(0);
				Boolean isEqual = this.isAddressEqualToSuggested(address, suggestedAddress);
				if (Boolean.TRUE.equals(isEqual)) {
					return isEqual;
				}
				
			}
		} else if (setting == PoolFilterSetting.VALID_ONLY) {
			if (!validator.isSerpValid()) {
				logger.debug("This address is not SERP valid, so skipping");
				return Boolean.TRUE;
			}
		} else if (setting == PoolFilterSetting.INVALID_OR_DIFFERENT_FORMAT) {
			logger.debug("Accepting only SERP invalid addresses or addresses with suggestions");
			if (validator.isSerpValid()) {
				List<Address> suggestions = validator.getSuggestions();
				// if no suggestions, then skip
				if (suggestions.size() == 0) {
					logger.debug("This address is SERP valid, or has no suggestions, so skipping");
					return Boolean.TRUE;
				}
				// if only one suggestion and it's the same as the original, then skip
				if (suggestions.size() == 1) {
					Address suggestedAddress = suggestions.get(0);
					Boolean isEqual = this.isAddressEqualToSuggested(address, suggestedAddress);
					if (Boolean.TRUE.equals(isEqual)) {
						return isEqual;
					}
				}
			}
		} else if (setting == PoolFilterSetting.VALID_OR_DIFFERENT_FORMAT) {
			logger.debug("Accepting only SERP invalid addresses or addresses with suggestions");
			if (!validator.isSerpValid()) {
				logger.debug("This address is SERP invalid, so skipping");
				return Boolean.TRUE;
			}
		}
		
		SQLIndex uniqueKey = getProject().getSourceTableIndex();
	
		MungeStep inputStep = getInputStep();
		
		List<Object> uniqueKeyValues = new ArrayList<Object>();
		
		for (Column col: uniqueKey.getChildren(Column.class)) {
			MungeStepOutput output = inputStep.getOutputByName(col.getName());
			if (output == null) {
				throw new IllegalStateException("Input step is missing unique key column '" + col.getName() + "'");
			}
			uniqueKeyValues.add(output.getData());
		}
		
		AddressResult result = new AddressResult(uniqueKeyValues, addressLine1, addressLine2, municipality, province, inPostalCode, country);
		
		AutoValidateSetting autoValidateSetting = getProject().getMungeSettings().getAutoValidateSetting();
		switch (autoValidateSetting) {
			case NOTHING:
				logger.debug("Autovalidation disabled");
				break;
			case SERP_CORRECTABLE:
				logger.debug("Autovalidating SERP correctable addresses");
				if (validator.isSerpValid() || !validator.isValidSuggestion()) {
					logger.debug("Address is SERP valid, or has no valid suggestions, so skipping");
					break;
				}
			case EVERYTHING_WITH_ONE_SUGGESTION:
				logger.debug("Autovalidating anything with just one suggestion");
				if (!validator.isValidSuggestion() || (validator.getSuggestions().size() != 1 && autoValidateSetting == AutoValidateSetting.EVERYTHING_WITH_ONE_SUGGESTION)) {
					logger.debug("Validator has zero or more than one suggestion, so skipping");
					break;
				}
			case EVERYTHING_WITH_SUGGESTION:
				logger.debug("Autovalidating anything with a suggestion");
				if (!validator.isValidSuggestion() || validator.getSuggestions().size() == 0) {
					logger.debug("Validator has no suggestions, so skipping");
					break;
				}
			default:
				if (getProject().getMungeSettings().isAutoWriteAutoValidatedAddresses()) {
					logger.debug("Automatically writing back an auto-validated address");
					Address correctedAddress = validator.getSuggestions().get(0);
					logger.debug("Replacing address \n" + address + " with \n" + correctedAddress);
					
					logger.debug("Top suggestion from validator is: " + correctedAddress);
					
					List<MungeStepOutput> outputs = getChildren(MungeStepOutput.class); 
					
					outputs.get(0).setData(correctedAddress.getAddress());
					outputs.get(1).setData(addressLine2);
					outputs.get(2).setData(correctedAddress.getSuite());
					outputs.get(3).setData(correctedAddress.getStreetNumber() != null ? BigDecimal.valueOf(correctedAddress.getStreetNumber()) : null);
					outputs.get(4).setData(correctedAddress.getStreetNumberSuffix());
					outputs.get(5).setData(correctedAddress.getStreet());
					outputs.get(6).setData(correctedAddress.getStreetType());
					outputs.get(7).setData(correctedAddress.getStreetDirection());
					outputs.get(8).setData(correctedAddress.getMunicipality());
					outputs.get(9).setData(correctedAddress.getProvince());
					outputs.get(10).setData(country);
					outputs.get(11).setData(correctedAddress.getPostalCode());
					outputs.get(12).setData(validator.isSerpValid());
					
					addressCorrected = true;
					
					return Boolean.TRUE;
				}  
				
				logger.debug("Autovalidating address to the following address: " + validator.getSuggestions().get(0));
				result.setOutputAddress(validator.getSuggestions().get(0));
				result.setValid(true);
		}
		pool.addAddress(result, logger);
		
		return Boolean.TRUE;
	}

	/**
	 * A package-private method that will return whether or not the current
	 * address that this step has set as its output is corrected. This means
	 * that the address inside was either automatically SERP corrected or it is
	 * placed an address from the address result pool that is marked as 'valid'.
	 * <p>
	 * Note that the value is meaningless if there is no address currently being
	 * parsed in this step. Generally, the boolean value applies to the address
	 * data it received that last time the {@link #doCall()} method was called.
	 */
	@Transient @Accessor
	boolean isAddressCorrected() {
		return addressCorrected;
	}
	
	@NonBound
	MungeStep getInputStep() {
		return inputStep;
	}
	
	@NonBound
	private void setAddressDB(AddressDatabase addressDB) {
		AddressDatabase oldValue = this.addressDB;
		this.addressDB = addressDB;
		//XXX: Firing this event would be better than the munge component listening to the context
//		getEventSupport().firePropertyChange("addressDB", oldValue, addressDB);
	}
	
	public boolean doesDatabaseExist()  {
		return addressDB != null;
	}
	
	@Transient @Mutator
	void setAddressPool(AddressPool pool, Logger logger) {
		this.pool = pool;
	}
	
	@Override
	public List<ValidateResult> checkPreconditions() {
		List<ValidateResult> resultList = new ArrayList<ValidateResult>();
		if (addressDB == null) {
			resultList.add(ValidateResult
					.createValidateResult(
							Status.FAIL,
							"Address data is not valid or not setup properly. " +
							"Please check the Address Database Path in User Preferences."));
		}
		return resultList;
	}

	/**
	 * Returns the {@link AddressStatus} of the last address processed by this
	 * {@link AddressCorrectionMungeStep}. If it is null, then no addresses have
	 * been processed by this step yet.
	 */
	@Transient @Accessor
	public AddressStatus getAddressStatus() {
		return addressStatus;
	}

	@Mutator
	public void setAddressCorrectionDataPath(String addressCorrectionDataPath) {
		String oldPath = this.addressCorrectionDataPath;
		this.addressCorrectionDataPath = addressCorrectionDataPath;
		firePropertyChange("addressCorrectionDataPath", oldPath, addressCorrectionDataPath);
	}

	@Accessor
	public String getAddressCorrectionDataPath() {
		return addressCorrectionDataPath;
	}
	
	@Override
	protected void copyPropertiesForDuplicate(MungeStep copy) {
		((AddressCorrectionMungeStep) copy).setAddressCorrectionDataPath(getAddressCorrectionDataPath());
	}
}
