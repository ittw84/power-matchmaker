// $ANTLR 3.2 Sep 23, 2009 12:02:23 /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g 2009-11-12 20:35:04

package ca.sqlpower.matchmaker.address.parse;

import java.util.List;
import java.util.Set;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.PostalCode;

import com.sleepycat.je.DatabaseException;

public class AddressParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "STREETNUMSUFFIX", "NUMERICSTREETSUFFIX", "NUMANDSUFFIX", "NUMBER", "NAME", "SUITEANDSTREETNUM", "WS", "'-'", "'#'"
    };
    public static final int SUITEANDSTREETNUM=9;
    public static final int NAME=8;
    public static final int WS=10;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int NUMBER=7;
    public static final int NUMANDSUFFIX=6;
    public static final int NUMERICSTREETSUFFIX=5;
    public static final int EOF=-1;
    public static final int STREETNUMSUFFIX=4;

    // delegates
    // delegators


        public AddressParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public AddressParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return AddressParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g"; }


    private static final Logger logger = Logger.getLogger(AddressParser.class);

    /**
     *  This is an odd tri state boolean. It will be null if the address starts as
     *  anything but an urban or rural address. It will be true if it starts as an
     *  urban address and false if it starts as a rural address. Only urban and rural
     *  addresses should care about this as it is used to distinguish between rural,
     *  urban, and mixed addresses. The fun part is mixed addresses can start as urban
     *  or rural and some parts from one could possiblly come up in the other.
     */
    private Boolean startsUrbanNotRural = null;

    private Set<PostalCode> postalCodes;

    private AddressDatabase addressDatabase;

    private Address address = new Address();

    /**
     * This may be moved into a local variable later
     */
     private boolean hasStreetNameStarted = false;

    public Address getAddress() { return address; }

    public void setAddressDatabase(AddressDatabase addressDatabase) {
       this.addressDatabase = addressDatabase;
    }

    public void setPostalCode(String postalCodeString) throws DatabaseException {
       if (addressDatabase == null) throw new NullPointerException("No address database!");
       postalCodes = addressDatabase.findPostalCode(postalCodeString);
    }

    private boolean couldBeUrban() {
       if (postalCodes.isEmpty()) return true;
       boolean isUrbanType = false;
       for (PostalCode postalCode : postalCodes) {
          isUrbanType |= postalCode.getRecordType() == PostalCode.RecordType.STREET || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
       }
       return isUrbanType;
    }

    private boolean couldBeRural() {
       if (postalCodes.isEmpty()) return true;
       boolean isRuralType = false;
       for (PostalCode postalCode : postalCodes) {
          isRuralType |= postalCode.getRecordType() == PostalCode.RecordType.ROUTE || postalCode.getRecordType() == PostalCode.RecordType.STREET_AND_ROUTE;
       }
       return isRuralType;
    }

    private boolean couldBeLockBox() {
       if (postalCodes.isEmpty()) return true;
       boolean isLockBoxType = false;
       for (PostalCode postalCode : postalCodes) {
          isLockBoxType |= postalCode.getRecordType() == PostalCode.RecordType.LOCK_BOX;
       }
       return isLockBoxType;
    }

    private boolean couldBeGD() {
       if (postalCodes.isEmpty()) return true;
       boolean isGDType = false;
       for (PostalCode postalCode : postalCodes) {
          isGDType |= postalCode.getRecordType() == PostalCode.RecordType.GENERAL_DELIVERY;
       }
       return isGDType;
    }

    /**
     * Parses an integer value from a string, failing silently and returning
     * null if the value is not parseable.
     */
    private Integer quietIntParse(String s) {
      if (s == null) return null;
      try {
        return Integer.valueOf(s);
      } catch (NumberFormatException ex) {
        if (s.charAt(0) == '#') {
          try {
            return Integer.valueOf(s.substring(1, s.length()));
          } catch (NumberFormatException ex1) {
            //return default value
          }
        }
        return null;
      }
    }

    private void appendStreetName(String name) {
       if (address.getStreet() != null && address.getStreet().trim().length() > 0) {
          address.setStreet(address.getStreet() + " " + name);
       } else {
          address.setStreet(name);
       }
    }

    public String getErrorMessage(RecognitionException e, 
    String[] tokenNames) 
    { 
    List stack = getRuleInvocationStack(e, this.getClass().getName()); 
    String msg = null; 
    if ( e instanceof NoViableAltException ) { 
    NoViableAltException nvae = (NoViableAltException)e; 
    msg = " no viable alt; token="+e.token+ 
    " (decision="+nvae.decisionNumber+ 
    " state "+nvae.stateNumber+")"+ 
    " decision=<<"+nvae.grammarDecisionDescription+">>"; 
    } 
    else { 
    msg = super.getErrorMessage(e, tokenNames); 
    } 
    return stack+" "+msg; 
    } 
    public String getTokenErrorDisplay(Token t) { 
    return t.toString(); 
    } 

    //XXX: might be able to use a variable in some kind of global scope
    public boolean setStartsUrbanNotRural(boolean b) {
      startsUrbanNotRural = b;
      return true;
    }
     
    @Override
    public void emitErrorMessage(String msg) {
      logger.debug("ANTLR Error Message: " + msg);
    }




    // $ANTLR start "address"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:164:1: address : ( streetAddressStart | ruralRouteAddress | lockBoxAddress | generalDeliveryAddress | failedParse );
    public final void address() throws RecognitionException {
        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:165:2: ( streetAddressStart | ruralRouteAddress | lockBoxAddress | generalDeliveryAddress | failedParse )
            int alt1=5;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:165:4: streetAddressStart
                    {
                    pushFollow(FOLLOW_streetAddressStart_in_address41);
                    streetAddressStart();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:166:4: ruralRouteAddress
                    {
                    pushFollow(FOLLOW_ruralRouteAddress_in_address47);
                    ruralRouteAddress();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:167:4: lockBoxAddress
                    {
                    pushFollow(FOLLOW_lockBoxAddress_in_address52);
                    lockBoxAddress();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:168:4: generalDeliveryAddress
                    {
                    pushFollow(FOLLOW_generalDeliveryAddress_in_address57);
                    generalDeliveryAddress();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:169:4: failedParse
                    {
                    pushFollow(FOLLOW_failedParse_in_address63);
                    failedParse();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "address"


    // $ANTLR start "failedParse"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:172:1: failedParse : ( failedToken )* ;
    public final void failedParse() throws RecognitionException {
        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:2: ( ( failedToken )* )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:4: ( failedToken )*
            {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:4: ( failedToken )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=STREETNUMSUFFIX && LA2_0<=NAME)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:173:4: failedToken
            	    {
            	    pushFollow(FOLLOW_failedToken_in_failedParse79);
            	    failedToken();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "failedParse"


    // $ANTLR start "failedToken"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:176:1: failedToken : n= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME ) ;
    public final void failedToken() throws RecognitionException {
        Token n=null;

        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:177:2: (n= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME ) )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:177:4: n= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX | NUMANDSUFFIX | NUMBER | NAME )
            {
            n=(Token)input.LT(1);
            if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NAME) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            							 if (address.getFailedParsingString() == null) {
            							    address.setFailedParsingString((n!=null?n.getText():null));
            							 } else {
            							    address.setFailedParsingString(address.getFailedParsingString() + " " + (n!=null?n.getText():null));
            							 }
            							

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "failedToken"


    // $ANTLR start "streetAddressStart"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:186:1: streetAddressStart : {...}? streetAddress ;
    public final void streetAddressStart() throws RecognitionException {
        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:187:2: ({...}? streetAddress )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:187:4: {...}? streetAddress
            {
            if ( !((setStartsUrbanNotRural(true))) ) {
                throw new FailedPredicateException(input, "streetAddressStart", "setStartsUrbanNotRural(true)");
            }
            pushFollow(FOLLOW_streetAddress_in_streetAddressStart132);
            streetAddress();

            state._fsp--;


            							  address.setType(PostalCode.RecordType.STREET);
            							  if (address.isUrbanBeforeRural() != null) {
            							    address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
            							  }
            							

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "streetAddressStart"


    // $ANTLR start "streetAddress"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:196:1: streetAddress : (sn= NUMBER '-' street | street );
    public final void streetAddress() throws RecognitionException {
        Token sn=null;

        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:197:2: (sn= NUMBER '-' street | street )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==NUMBER) ) {
                int LA3_1 = input.LA(2);

                if ( (LA3_1==11) ) {
                    alt3=1;
                }
                else if ( ((LA3_1>=STREETNUMSUFFIX && LA3_1<=NAME)) ) {
                    alt3=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA3_0==NUMANDSUFFIX||LA3_0==SUITEANDSTREETNUM) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:197:4: sn= NUMBER '-' street
                    {
                    sn=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetAddress160); 
                    match(input,11,FOLLOW_11_in_streetAddress162); 
                    pushFollow(FOLLOW_street_in_streetAddress164);
                    street();

                    state._fsp--;

                     
                    							  address.setSuitePrefix(true);
                    							  address.setSuite((sn!=null?sn.getText():null));

                    							

                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:203:4: street
                    {
                    pushFollow(FOLLOW_street_in_streetAddress181);
                    street();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "streetAddress"


    // $ANTLR start "street"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:206:1: street : (n= SUITEANDSTREETNUM ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= NUMBER ( streetToken )+ );
    public final void street() throws RecognitionException {
        Token n=null;

        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:2: (n= SUITEANDSTREETNUM ( streetToken )+ | n= NUMANDSUFFIX ( streetToken )+ | n= NUMBER ( streetToken )+ )
            int alt7=3;
            switch ( input.LA(1) ) {
            case SUITEANDSTREETNUM:
                {
                alt7=1;
                }
                break;
            case NUMANDSUFFIX:
                {
                alt7=2;
                }
                break;
            case NUMBER:
                {
                alt7=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }

            switch (alt7) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:4: n= SUITEANDSTREETNUM ( streetToken )+
                    {
                    n=(Token)match(input,SUITEANDSTREETNUM,FOLLOW_SUITEANDSTREETNUM_in_street195); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:24: ( streetToken )+
                    int cnt4=0;
                    loop4:
                    do {
                        int alt4=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt4=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt4=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                            {
                            alt4=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt4=1;
                            }
                            break;
                        case NUMANDSUFFIX:
                            {
                            alt4=1;
                            }
                            break;

                        }

                        switch (alt4) {
                    	case 1 :
                    	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:207:24: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street197);
                    	    streetToken();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt4 >= 1 ) break loop4;
                                EarlyExitException eee =
                                    new EarlyExitException(4, input);
                                throw eee;
                        }
                        cnt4++;
                    } while (true);

                    String[] numbers = (n!=null?n.getText():null).split("-");
                    							 address.setSuitePrefix(true);
                    							 address.setSuite(numbers[0]);
                    							 address.setStreetNumber(quietIntParse(numbers[1]));
                    							

                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:213:4: n= NUMANDSUFFIX ( streetToken )+
                    {
                    n=(Token)match(input,NUMANDSUFFIX,FOLLOW_NUMANDSUFFIX_in_street214); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:213:19: ( streetToken )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt5=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt5=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                            {
                            alt5=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt5=1;
                            }
                            break;
                        case NUMANDSUFFIX:
                            {
                            alt5=1;
                            }
                            break;

                        }

                        switch (alt5) {
                    	case 1 :
                    	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:213:19: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street216);
                    	    streetToken();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt5 >= 1 ) break loop5;
                                EarlyExitException eee =
                                    new EarlyExitException(5, input);
                                throw eee;
                        }
                        cnt5++;
                    } while (true);

                    String streetNum = (n!=null?n.getText():null);
                    							 address.setStreetNumber(quietIntParse(streetNum.substring(0, streetNum.length() - 1)));
                    							 address.setStreetNumberSuffix(streetNum.substring(streetNum.length() - 1, streetNum.length()));
                    							 address.setStreetNumberSuffixSeparate(false);
                    							

                    }
                    break;
                case 3 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:4: n= NUMBER ( streetToken )+
                    {
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_street227); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:13: ( streetToken )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt6=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt6=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                            {
                            alt6=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt6=1;
                            }
                            break;
                        case NUMANDSUFFIX:
                            {
                            alt6=1;
                            }
                            break;

                        }

                        switch (alt6) {
                    	case 1 :
                    	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:218:13: streetToken
                    	    {
                    	    pushFollow(FOLLOW_streetToken_in_street229);
                    	    streetToken();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt6 >= 1 ) break loop6;
                                EarlyExitException eee =
                                    new EarlyExitException(6, input);
                                throw eee;
                        }
                        cnt6++;
                    } while (true);

                    address.setStreetNumber(quietIntParse((n!=null?n.getText():null)));

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "street"


    // $ANTLR start "streetToken"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:222:1: streetToken : ({...}? =>s= NAME sn= NUMBER | {...}? =>t= ( NAME | STREETNUMSUFFIX ) | {...}? =>d= ( NAME | STREETNUMSUFFIX ) | {...}? =>s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX ) | {...}? =>n= NUMBER | {...}? => ruralRoute | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) );
    public final void streetToken() throws RecognitionException {
        Token s=null;
        Token sn=null;
        Token t=null;
        Token d=null;
        Token n=null;

        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:2: ({...}? =>s= NAME sn= NUMBER | {...}? =>t= ( NAME | STREETNUMSUFFIX ) | {...}? =>d= ( NAME | STREETNUMSUFFIX ) | {...}? =>s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX ) | {...}? =>n= NUMBER | {...}? => ruralRoute | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) )
            int alt8=7;
            alt8 = dfa8.predict(input);
            switch (alt8) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:223:4: {...}? =>s= NAME sn= NUMBER
                    {
                    if ( !((Address.isSuiteType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "Address.isSuiteType(input.LT(1).getText())");
                    }
                    s=(Token)match(input,NAME,FOLLOW_NAME_in_streetToken259); 
                    sn=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetToken263); 

                    							 address.setSuitePrefix(false);
                    							 address.setSuiteType((s!=null?s.getText():null));
                    							 address.setSuite((sn!=null?sn.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:230:4: {...}? =>t= ( NAME | STREETNUMSUFFIX )
                    {
                    if ( !(((!address.isStreetTypePrefix() || ("C".equals(address.getStreetType()) && address.getStreetNumberSuffix() == null)) && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "(!address.isStreetTypePrefix() || (\"C\".equals(address.getStreetType()) && address.getStreetNumberSuffix() == null)) && addressDatabase.containsStreetType(input.LT(1).getText())");
                    }
                    t=(Token)input.LT(1);
                    if ( input.LA(1)==STREETNUMSUFFIX||input.LA(1)==NAME ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    							 //Fun special case where the street type C can come before the street name
                    							 //like a street type, somtimes it's a street type, sometimes it's a street
                    							 //number suffix. It's to be considered a street type unless there's another
                    							 //street type then it's a street number suffix if it comes after the street
                    							 //number (ie before the street name) and the street number suffix does not
                    							 //exist yet (may be a fun case of 118 C C Avenue = 118C Center Avenue).
                    							 if ("C".equals(address.getStreetType()) && address.getStreetNumberSuffix() == null) {
                    							    address.setStreetNumberSuffix("C");
                    							    address.setStreetType(null);
                    							    address.setStreetTypePrefix(false);
                    							 }
                    							 if (address.getStreetType() != null) {
                    							    appendStreetName(address.getStreetType());
                    							 }
                    							 if (!address.isSuitePrefix() && address.getSuite() != null) {
                    							    if (address.getSuiteType() != null) {
                    							       appendStreetName(address.getSuiteType());
                    							       address.setSuiteType(null);
                    							    }
                    							    appendStreetName(address.getSuite());
                    							    address.setSuite(null);
                    							 }
                    							 address.setStreetTypePrefix(!hasStreetNameStarted);
                    							 address.setStreetType((t!=null?t.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:258:4: {...}? =>d= ( NAME | STREETNUMSUFFIX )
                    {
                    if ( !((hasStreetNameStarted && Address.isStreetDirection(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "streetToken", "hasStreetNameStarted && Address.isStreetDirection(input.LT(1).getText())");
                    }
                    d=(Token)input.LT(1);
                    if ( input.LA(1)==STREETNUMSUFFIX||input.LA(1)==NAME ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    							 address.setStreetDirection((d!=null?d.getText():null));
                    							

                    }
                    break;
                case 4 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:263:4: {...}? =>s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX )
                    {
                    if ( !(((!hasStreetNameStarted) && address.getStreetType() == null)) ) {
                        throw new FailedPredicateException(input, "streetToken", "(!hasStreetNameStarted) && address.getStreetType() == null");
                    }
                    s=(Token)input.LT(1);
                    if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NUMERICSTREETSUFFIX) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    							 address.setStreetNumberSuffix((s!=null?s.getText():null));
                    							 address.setStreetNumberSuffixSeparate(true);
                    							

                    }
                    break;
                case 5 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:269:4: {...}? =>n= NUMBER
                    {
                    if ( !((hasStreetNameStarted)) ) {
                        throw new FailedPredicateException(input, "streetToken", "hasStreetNameStarted");
                    }
                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_streetToken384); 

                    							 address.setSuitePrefix(false);
                    							 address.setSuite((n!=null?n.getText():null));
                    							

                    }
                    break;
                case 6 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:275:4: {...}? => ruralRoute
                    {
                    if ( !((hasStreetNameStarted && startsUrbanNotRural)) ) {
                        throw new FailedPredicateException(input, "streetToken", "hasStreetNameStarted && startsUrbanNotRural");
                    }
                    pushFollow(FOLLOW_ruralRoute_in_streetToken409);
                    ruralRoute();

                    state._fsp--;


                    							 address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
                    							 address.setUrbanBeforeRural(true);
                    							

                    }
                    break;
                case 7 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:281:4: n= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX )
                    {
                    n=(Token)input.LT(1);
                    if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NAME) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    							 if (!address.isStreetTypePrefix() && address.getStreetType() != null) {
                    							    appendStreetName(address.getStreetType());
                    							    address.setStreetType(null);
                    							 }
                    							 if (address.getStreetDirection() != null) {
                    							    appendStreetName(address.getStreetDirection());
                    							    address.setStreetDirection(null);
                    							 }
                    							 if (!address.isSuitePrefix()) {
                    							    if (address.getSuiteType() != null) {
                    							       appendStreetName(address.getSuiteType());
                    							       address.setSuiteType(null);
                    							    }
                    							    if (address.getSuite() != null) {
                    							       appendStreetName(address.getSuite());
                    							       address.setSuite(null);
                    							    }
                    							 }
                    							 
                    							 hasStreetNameStarted = true;
                    							 appendStreetName((n!=null?n.getText():null));
                    							

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "streetToken"


    // $ANTLR start "ruralRouteAddress"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:307:1: ruralRouteAddress : {...}? ruralRoute ;
    public final void ruralRouteAddress() throws RecognitionException {
        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:308:2: ({...}? ruralRoute )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:308:4: {...}? ruralRoute
            {
            if ( !((setStartsUrbanNotRural(false))) ) {
                throw new FailedPredicateException(input, "ruralRouteAddress", "setStartsUrbanNotRural(false)");
            }
            pushFollow(FOLLOW_ruralRoute_in_ruralRouteAddress474);
            ruralRoute();

            state._fsp--;

            address.setType(PostalCode.RecordType.ROUTE);
            							  if (address.isUrbanBeforeRural() != null) {
            							    address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
            							  }
            							

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruralRouteAddress"


    // $ANTLR start "ruralRoute"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:316:1: ruralRoute : ({...}? =>rs= NAME (n= NUMBER )? ruralRouteSuffix | {...}? =>rs1= NAME rs2= NAME (n= NUMBER )? ruralRouteSuffix | {...}? =>rs1= NAME rs2= NAME rs3= NAME (n= NUMBER )? ruralRouteSuffix );
    public final void ruralRoute() throws RecognitionException {
        Token rs=null;
        Token n=null;
        Token rs1=null;
        Token rs2=null;
        Token rs3=null;

        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:317:2: ({...}? =>rs= NAME (n= NUMBER )? ruralRouteSuffix | {...}? =>rs1= NAME rs2= NAME (n= NUMBER )? ruralRouteSuffix | {...}? =>rs1= NAME rs2= NAME rs3= NAME (n= NUMBER )? ruralRouteSuffix )
            int alt12=3;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==NAME) && (((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isRuralRoute(input.LT(1).getText()))||(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1==NAME) && (((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isRuralRoute(input.LT(1).getText()))||(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                    int LA12_2 = input.LA(3);

                    if ( (LA12_2==NAME) && ((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())))) {
                        alt12=3;
                    }
                    else if ( ((Address.isRuralRoute(input.LT(1).getText()))) ) {
                        alt12=1;
                    }
                    else if ( ((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        alt12=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 12, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA12_1==EOF||(LA12_1>=STREETNUMSUFFIX && LA12_1<=NUMBER)||LA12_1==SUITEANDSTREETNUM) && ((Address.isRuralRoute(input.LT(1).getText())))) {
                    alt12=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:317:4: {...}? =>rs= NAME (n= NUMBER )? ruralRouteSuffix
                    {
                    if ( !((Address.isRuralRoute(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "ruralRoute", "Address.isRuralRoute(input.LT(1).getText())");
                    }
                    rs=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute504); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:317:62: (n= NUMBER )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==NUMBER) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:317:62: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRoute508); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_ruralRouteSuffix_in_ruralRoute511);
                    ruralRouteSuffix();

                    state._fsp--;


                    							 address.setRuralRouteType((rs!=null?rs.getText():null));
                    							 address.setRuralRouteNumber((n!=null?n.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:322:4: {...}? =>rs1= NAME rs2= NAME (n= NUMBER )? ruralRouteSuffix
                    {
                    if ( !((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "ruralRoute", "Address.isRuralRoute(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    rs1=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute530); 
                    rs2=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute534); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:322:102: (n= NUMBER )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==NUMBER) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:322:102: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRoute538); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_ruralRouteSuffix_in_ruralRoute541);
                    ruralRouteSuffix();

                    state._fsp--;


                    							 address.setRuralRouteType((rs1!=null?rs1.getText():null) + " " + (rs2!=null?rs2.getText():null));
                    							 address.setRuralRouteNumber((n!=null?n.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:327:4: {...}? =>rs1= NAME rs2= NAME rs3= NAME (n= NUMBER )? ruralRouteSuffix
                    {
                    if ( !((Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                        throw new FailedPredicateException(input, "ruralRoute", "Address.isRuralRoute(input.LT(1).getText() + \" \" + input.LT(2).getText() + \" \" + input.LT(3).getText())");
                    }
                    rs1=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute560); 
                    rs2=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute564); 
                    rs3=(Token)match(input,NAME,FOLLOW_NAME_in_ruralRoute568); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:327:141: (n= NUMBER )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==NUMBER) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:327:141: n= NUMBER
                            {
                            n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_ruralRoute572); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_ruralRouteSuffix_in_ruralRoute575);
                    ruralRouteSuffix();

                    state._fsp--;


                    							 address.setRuralRouteType((rs1!=null?rs1.getText():null) + " " + (rs2!=null?rs2.getText():null) + " " + (rs3!=null?rs3.getText():null));
                    							 address.setRuralRouteNumber((n!=null?n.getText():null));
                    							

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruralRoute"


    // $ANTLR start "ruralRouteSuffix"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:334:1: ruralRouteSuffix : ({...}? => streetAddress | diTypeAndName );
    public final void ruralRouteSuffix() throws RecognitionException {
        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:335:2: ({...}? => streetAddress | diTypeAndName )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==NUMBER) ) {
                int LA13_1 = input.LA(2);

                if ( ((!startsUrbanNotRural)) ) {
                    alt13=1;
                }
                else if ( (true) ) {
                    alt13=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA13_0==SUITEANDSTREETNUM) && ((!startsUrbanNotRural))) {
                alt13=1;
            }
            else if ( (LA13_0==NUMANDSUFFIX) ) {
                int LA13_3 = input.LA(2);

                if ( ((!startsUrbanNotRural)) ) {
                    alt13=1;
                }
                else if ( (true) ) {
                    alt13=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 3, input);

                    throw nvae;
                }
            }
            else if ( (LA13_0==EOF||(LA13_0>=STREETNUMSUFFIX && LA13_0<=NUMERICSTREETSUFFIX)||LA13_0==NAME) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:335:4: {...}? => streetAddress
                    {
                    if ( !((!startsUrbanNotRural)) ) {
                        throw new FailedPredicateException(input, "ruralRouteSuffix", "!startsUrbanNotRural");
                    }
                    pushFollow(FOLLOW_streetAddress_in_ruralRouteSuffix598);
                    streetAddress();

                    state._fsp--;


                    							 address.setType(PostalCode.RecordType.STREET_AND_ROUTE);
                    							 address.setUrbanBeforeRural(false);
                    							

                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:340:4: diTypeAndName
                    {
                    pushFollow(FOLLOW_diTypeAndName_in_ruralRouteSuffix616);
                    diTypeAndName();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruralRouteSuffix"


    // $ANTLR start "lockBoxAddress"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:343:1: lockBoxAddress : ({...}? =>lb= NAME ( '#' )? n= NUMBER diTypeAndName | {...}? =>lb1= NAME lb2= NAME ( '#' )? n= NUMBER diTypeAndName );
    public final void lockBoxAddress() throws RecognitionException {
        Token lb=null;
        Token n=null;
        Token lb1=null;
        Token lb2=null;

        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:344:2: ({...}? =>lb= NAME ( '#' )? n= NUMBER diTypeAndName | {...}? =>lb1= NAME lb2= NAME ( '#' )? n= NUMBER diTypeAndName )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==NAME) && (((Address.isLockBox(input.LT(1).getText()))||(Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                int LA16_1 = input.LA(2);

                if ( (LA16_1==NAME) && ((Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText())))) {
                    alt16=2;
                }
                else if ( (LA16_1==NUMBER||LA16_1==12) && ((Address.isLockBox(input.LT(1).getText())))) {
                    alt16=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 16, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:344:4: {...}? =>lb= NAME ( '#' )? n= NUMBER diTypeAndName
                    {
                    if ( !((Address.isLockBox(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "lockBoxAddress", "Address.isLockBox(input.LT(1).getText())");
                    }
                    lb=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress633); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:344:58: ( '#' )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==12) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:344:58: '#'
                            {
                            match(input,12,FOLLOW_12_in_lockBoxAddress635); 

                            }
                            break;

                    }

                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress640); 
                    pushFollow(FOLLOW_diTypeAndName_in_lockBoxAddress642);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setLockBoxType((lb!=null?lb.getText():null));
                    							 address.setLockBoxNumber((n!=null?n.getText():null));
                    							 address.setType(PostalCode.RecordType.LOCK_BOX);
                    							

                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:350:4: {...}? =>lb1= NAME lb2= NAME ( '#' )? n= NUMBER diTypeAndName
                    {
                    if ( !((Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "lockBoxAddress", "Address.isLockBox(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    lb1=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress661); 
                    lb2=(Token)match(input,NAME,FOLLOW_NAME_in_lockBoxAddress665); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:350:98: ( '#' )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==12) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:350:98: '#'
                            {
                            match(input,12,FOLLOW_12_in_lockBoxAddress667); 

                            }
                            break;

                    }

                    n=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lockBoxAddress672); 
                    pushFollow(FOLLOW_diTypeAndName_in_lockBoxAddress674);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setLockBoxType((lb1!=null?lb1.getText():null) + " " + (lb2!=null?lb2.getText():null));
                    							 address.setLockBoxNumber((n!=null?n.getText():null));
                    							 address.setType(PostalCode.RecordType.LOCK_BOX);
                    							

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "lockBoxAddress"


    // $ANTLR start "generalDeliveryAddress"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:358:1: generalDeliveryAddress : ({...}? =>gd= NAME diTypeAndName | {...}? =>gd1= NAME gd2= NAME diTypeAndName | {...}? =>gd1= NAME gd2= ( STREETNUMSUFFIX | NAME ) gd3= NAME diTypeAndName );
    public final void generalDeliveryAddress() throws RecognitionException {
        Token gd=null;
        Token gd1=null;
        Token gd2=null;
        Token gd3=null;

        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:359:2: ({...}? =>gd= NAME diTypeAndName | {...}? =>gd1= NAME gd2= NAME diTypeAndName | {...}? =>gd1= NAME gd2= ( STREETNUMSUFFIX | NAME ) gd3= NAME diTypeAndName )
            int alt17=3;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==NAME) && (((Address.isGeneralDelivery(input.LT(1).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                int LA17_1 = input.LA(2);

                if ( (LA17_1==NAME) && (((Address.isGeneralDelivery(input.LT(1).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                    int LA17_2 = input.LA(3);

                    if ( (LA17_2==NAME) && ((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())))) {
                        alt17=3;
                    }
                    else if ( ((Address.isGeneralDelivery(input.LT(1).getText()))) ) {
                        alt17=1;
                    }
                    else if ( ((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        alt17=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA17_1==STREETNUMSUFFIX) && (((Address.isGeneralDelivery(input.LT(1).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))))) {
                    int LA17_3 = input.LA(3);

                    if ( (LA17_3==NAME) && (((Address.isGeneralDelivery(input.LT(1).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))))) {
                        int LA17_7 = input.LA(4);

                        if ( ((Address.isGeneralDelivery(input.LT(1).getText()))) ) {
                            alt17=1;
                        }
                        else if ( ((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                            alt17=3;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 17, 7, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA17_3==EOF||(LA17_3>=STREETNUMSUFFIX && LA17_3<=NUMBER)) && ((Address.isGeneralDelivery(input.LT(1).getText())))) {
                        alt17=1;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 17, 3, input);

                        throw nvae;
                    }
                }
                else if ( (LA17_1==EOF||(LA17_1>=NUMERICSTREETSUFFIX && LA17_1<=NUMBER)) && ((Address.isGeneralDelivery(input.LT(1).getText())))) {
                    alt17=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:359:4: {...}? =>gd= NAME diTypeAndName
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText())");
                    }
                    gd=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress700); 
                    pushFollow(FOLLOW_diTypeAndName_in_generalDeliveryAddress702);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setGeneralDeliveryName((gd!=null?gd.getText():null));
                    							 address.setType(PostalCode.RecordType.GENERAL_DELIVERY);
                    							

                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:364:4: {...}? =>gd1= NAME gd2= NAME diTypeAndName
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    gd1=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress721); 
                    gd2=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress725); 
                    pushFollow(FOLLOW_diTypeAndName_in_generalDeliveryAddress727);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setGeneralDeliveryName((gd1!=null?gd1.getText():null) + " " + (gd2!=null?gd2.getText():null));
                    							 address.setType(PostalCode.RecordType.GENERAL_DELIVERY);
                    							

                    }
                    break;
                case 3 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:369:4: {...}? =>gd1= NAME gd2= ( STREETNUMSUFFIX | NAME ) gd3= NAME diTypeAndName
                    {
                    if ( !((Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                        throw new FailedPredicateException(input, "generalDeliveryAddress", "Address.isGeneralDelivery(input.LT(1).getText() + \" \" + input.LT(2).getText() + \" \" + input.LT(3).getText())");
                    }
                    gd1=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress750); 
                    gd2=(Token)input.LT(1);
                    if ( input.LA(1)==STREETNUMSUFFIX||input.LA(1)==NAME ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    gd3=(Token)match(input,NAME,FOLLOW_NAME_in_generalDeliveryAddress762); 
                    pushFollow(FOLLOW_diTypeAndName_in_generalDeliveryAddress764);
                    diTypeAndName();

                    state._fsp--;


                    							 address.setGeneralDeliveryName((gd1!=null?gd1.getText():null) + " " + (gd2!=null?gd2.getText():null) + " " + (gd3!=null?gd3.getText():null));
                    							 address.setType(PostalCode.RecordType.GENERAL_DELIVERY);
                    							

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "generalDeliveryAddress"


    // $ANTLR start "diTypeAndName"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:377:1: diTypeAndName : ({...}? =>dt= NAME ( diName )* | {...}? =>dt1= NAME dt2= NAME ( diName )* | {...}? =>dt1= NAME dt2= NAME dt3= NAME ( diName )* | ( diName )* );
    public final void diTypeAndName() throws RecognitionException {
        Token dt=null;
        Token dt1=null;
        Token dt2=null;
        Token dt3=null;

        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:378:2: ({...}? =>dt= NAME ( diName )* | {...}? =>dt1= NAME dt2= NAME ( diName )* | {...}? =>dt1= NAME dt2= NAME dt3= NAME ( diName )* | ( diName )* )
            int alt22=4;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==NAME) ) {
                int LA22_1 = input.LA(2);

                if ( (LA22_1==NAME) && (((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                    int LA22_3 = input.LA(3);

                    if ( (LA22_3==NAME) && (((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))))) {
                        int LA22_5 = input.LA(4);

                        if ( ((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                            alt22=2;
                        }
                        else if ( ((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                            alt22=3;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 22, 5, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA22_3==EOF||(LA22_3>=STREETNUMSUFFIX && LA22_3<=NUMBER)) && ((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText())))) {
                        alt22=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 22, 3, input);

                        throw nvae;
                    }
                }
                else if ( ((Address.isDeliveryInstallationType(input.LT(1).getText()))) ) {
                    alt22=1;
                }
                else if ( (true) ) {
                    alt22=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 22, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA22_0==EOF||(LA22_0>=STREETNUMSUFFIX && LA22_0<=NUMBER)) ) {
                alt22=4;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:378:4: {...}? =>dt= NAME ( diName )*
                    {
                    if ( !((Address.isDeliveryInstallationType(input.LT(1).getText()))) ) {
                        throw new FailedPredicateException(input, "diTypeAndName", "Address.isDeliveryInstallationType(input.LT(1).getText())");
                    }
                    dt=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName790); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:378:75: ( diName )*
                    loop18:
                    do {
                        int alt18=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt18=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt18=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                            {
                            alt18=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt18=1;
                            }
                            break;
                        case NUMANDSUFFIX:
                            {
                            alt18=1;
                            }
                            break;

                        }

                        switch (alt18) {
                    	case 1 :
                    	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:378:75: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName792);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop18;
                        }
                    } while (true);


                    							 address.setDeliveryInstallationType((dt!=null?dt.getText():null));
                    							

                    }
                    break;
                case 2 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:382:4: {...}? =>dt1= NAME dt2= NAME ( diName )*
                    {
                    if ( !((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText()))) ) {
                        throw new FailedPredicateException(input, "diTypeAndName", "Address.isDeliveryInstallationType(input.LT(1).getText() + \" \" + input.LT(2).getText())");
                    }
                    dt1=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName812); 
                    dt2=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName816); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:382:115: ( diName )*
                    loop19:
                    do {
                        int alt19=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt19=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt19=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                            {
                            alt19=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt19=1;
                            }
                            break;
                        case NUMANDSUFFIX:
                            {
                            alt19=1;
                            }
                            break;

                        }

                        switch (alt19) {
                    	case 1 :
                    	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:382:115: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName818);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop19;
                        }
                    } while (true);


                    							 address.setDeliveryInstallationType((dt1!=null?dt1.getText():null) + " " + (dt2!=null?dt2.getText():null));
                    							

                    }
                    break;
                case 3 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:386:4: {...}? =>dt1= NAME dt2= NAME dt3= NAME ( diName )*
                    {
                    if ( !((Address.isDeliveryInstallationType(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))) ) {
                        throw new FailedPredicateException(input, "diTypeAndName", "Address.isDeliveryInstallationType(input.LT(1).getText() + \" \" + input.LT(2).getText() + \" \" + input.LT(3).getText())");
                    }
                    dt1=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName838); 
                    dt2=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName842); 
                    dt3=(Token)match(input,NAME,FOLLOW_NAME_in_diTypeAndName846); 
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:386:154: ( diName )*
                    loop20:
                    do {
                        int alt20=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt20=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt20=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                            {
                            alt20=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt20=1;
                            }
                            break;
                        case NUMANDSUFFIX:
                            {
                            alt20=1;
                            }
                            break;

                        }

                        switch (alt20) {
                    	case 1 :
                    	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:386:154: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName848);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop20;
                        }
                    } while (true);


                    							 address.setDeliveryInstallationType((dt1!=null?dt1.getText():null) + " " + (dt2!=null?dt2.getText():null) + " " + (dt3!=null?dt3.getText():null));
                    							

                    }
                    break;
                case 4 :
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:390:4: ( diName )*
                    {
                    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:390:4: ( diName )*
                    loop21:
                    do {
                        int alt21=2;
                        switch ( input.LA(1) ) {
                        case NAME:
                            {
                            alt21=1;
                            }
                            break;
                        case STREETNUMSUFFIX:
                            {
                            alt21=1;
                            }
                            break;
                        case NUMERICSTREETSUFFIX:
                            {
                            alt21=1;
                            }
                            break;
                        case NUMBER:
                            {
                            alt21=1;
                            }
                            break;
                        case NUMANDSUFFIX:
                            {
                            alt21=1;
                            }
                            break;

                        }

                        switch (alt21) {
                    	case 1 :
                    	    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:390:4: diName
                    	    {
                    	    pushFollow(FOLLOW_diName_in_diTypeAndName863);
                    	    diName();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop21;
                        }
                    } while (true);


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "diTypeAndName"


    // $ANTLR start "diName"
    // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:393:1: diName : stn= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) ;
    public final void diName() throws RecognitionException {
        Token stn=null;

        try {
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:394:2: (stn= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) )
            // /Users/jeffrey/Documents/workspace/matchmaker/src/ca/sqlpower/matchmaker/address/parse/Address.g:394:4: stn= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX )
            {
            stn=(Token)input.LT(1);
            if ( (input.LA(1)>=STREETNUMSUFFIX && input.LA(1)<=NAME) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            							 if (address.getDeliveryInstallationName() == null) {
            							    address.setDeliveryInstallationName((stn!=null?stn.getText():null));
            							 } else {
            							    address.setDeliveryInstallationName(address.getDeliveryInstallationName() + " " + (stn!=null?stn.getText():null));
            							 }
            							

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "diName"

    // Delegated rules


    protected DFA1 dfa1 = new DFA1(this);
    protected DFA8 dfa8 = new DFA8(this);
    static final String DFA1_eotS =
        "\24\uffff";
    static final String DFA1_eofS =
        "\2\5\1\uffff\1\5\20\uffff";
    static final String DFA1_minS =
        "\2\4\1\uffff\1\4\1\7\1\uffff\12\0\4\uffff";
    static final String DFA1_maxS =
        "\1\11\1\13\1\uffff\1\10\1\14\1\uffff\12\0\4\uffff";
    static final String DFA1_acceptS =
        "\2\uffff\1\1\2\uffff\1\5\12\uffff\2\3\1\2\1\4";
    static final String DFA1_specialS =
        "\4\uffff\1\7\1\uffff\1\2\1\4\1\11\1\1\1\12\1\6\1\10\1\3\1\0\1\5"+
        "\4\uffff}>";
    static final String[] DFA1_transitionS = {
            "\2\5\1\3\1\1\1\4\1\2",
            "\1\7\1\10\1\12\1\11\1\6\2\uffff\1\2",
            "",
            "\1\14\1\15\1\17\1\16\1\13",
            "\1\21\1\20\3\uffff\1\21",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);
    static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
    static final char[] DFA1_min = DFA.unpackEncodedStringToUnsignedChars(DFA1_minS);
    static final char[] DFA1_max = DFA.unpackEncodedStringToUnsignedChars(DFA1_maxS);
    static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);
    static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);
    static final short[][] DFA1_transition;

    static {
        int numStates = DFA1_transitionS.length;
        DFA1_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
        }
    }

    class DFA1 extends DFA {

        public DFA1(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 1;
            this.eot = DFA1_eot;
            this.eof = DFA1_eof;
            this.min = DFA1_min;
            this.max = DFA1_max;
            this.accept = DFA1_accept;
            this.special = DFA1_special;
            this.transition = DFA1_transition;
        }
        public String getDescription() {
            return "164:1: address : ( streetAddressStart | ruralRouteAddress | lockBoxAddress | generalDeliveryAddress | failedParse );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA1_14 = input.LA(1);

                         
                        int index1_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_14);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA1_9 = input.LA(1);

                         
                        int index1_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA1_6 = input.LA(1);

                         
                        int index1_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_6);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA1_13 = input.LA(1);

                         
                        int index1_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_13);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA1_7 = input.LA(1);

                         
                        int index1_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA1_15 = input.LA(1);

                         
                        int index1_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_15);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA1_11 = input.LA(1);

                         
                        int index1_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_11);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA1_4 = input.LA(1);

                         
                        int index1_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA1_4==NAME) && ((Address.isLockBox(input.LT(1).getText() + " " + input.LT(2).getText())))) {s = 16;}

                        else if ( (LA1_4==NUMBER||LA1_4==12) && ((Address.isLockBox(input.LT(1).getText())))) {s = 17;}

                        else if ( ((((setStartsUrbanNotRural(false))&&(Address.isRuralRoute(input.LT(1).getText())))||((setStartsUrbanNotRural(false))&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())))||((setStartsUrbanNotRural(false))&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText()))))) ) {s = 18;}

                        else if ( (((Address.isGeneralDelivery(input.LT(1).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText()))||(Address.isGeneralDelivery(input.LT(1).getText() + " " + input.LT(2).getText())))) ) {s = 19;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_4);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA1_12 = input.LA(1);

                         
                        int index1_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_12);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA1_8 = input.LA(1);

                         
                        int index1_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_8);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA1_10 = input.LA(1);

                         
                        int index1_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((setStartsUrbanNotRural(true))) ) {s = 2;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index1_10);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 1, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA8_eotS =
        "\14\uffff";
    static final String DFA8_eofS =
        "\14\uffff";
    static final String DFA8_minS =
        "\1\4\1\7\3\0\7\uffff";
    static final String DFA8_maxS =
        "\1\10\1\7\3\0\7\uffff";
    static final String DFA8_acceptS =
        "\5\uffff\1\7\1\1\1\2\1\3\1\6\1\4\1\5";
    static final String DFA8_specialS =
        "\1\uffff\1\3\1\0\1\1\1\2\7\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\2\1\3\1\5\1\4\1\1",
            "\1\6",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "222:1: streetToken : ({...}? =>s= NAME sn= NUMBER | {...}? =>t= ( NAME | STREETNUMSUFFIX ) | {...}? =>d= ( NAME | STREETNUMSUFFIX ) | {...}? =>s= ( STREETNUMSUFFIX | NUMERICSTREETSUFFIX ) | {...}? =>n= NUMBER | {...}? => ruralRoute | n= ( NAME | NUMBER | NUMANDSUFFIX | NUMERICSTREETSUFFIX | STREETNUMSUFFIX ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA8_2 = input.LA(1);

                         
                        int index8_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((!address.isStreetTypePrefix() || ("C".equals(address.getStreetType()) && address.getStreetNumberSuffix() == null)) && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {s = 7;}

                        else if ( ((hasStreetNameStarted && Address.isStreetDirection(input.LT(1).getText()))) ) {s = 8;}

                        else if ( (((!hasStreetNameStarted) && address.getStreetType() == null)) ) {s = 10;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index8_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA8_3 = input.LA(1);

                         
                        int index8_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((!hasStreetNameStarted) && address.getStreetType() == null)) ) {s = 10;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index8_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA8_4 = input.LA(1);

                         
                        int index8_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((hasStreetNameStarted)) ) {s = 11;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index8_4);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA8_1 = input.LA(1);

                         
                        int index8_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA8_1==NUMBER) && ((Address.isSuiteType(input.LT(1).getText())))) {s = 6;}

                        else if ( (((!address.isStreetTypePrefix() || ("C".equals(address.getStreetType()) && address.getStreetNumberSuffix() == null)) && addressDatabase.containsStreetType(input.LT(1).getText()))) ) {s = 7;}

                        else if ( ((hasStreetNameStarted && Address.isStreetDirection(input.LT(1).getText()))) ) {s = 8;}

                        else if ( ((((hasStreetNameStarted && startsUrbanNotRural)&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText() + " " + input.LT(3).getText())))||((hasStreetNameStarted && startsUrbanNotRural)&&(Address.isRuralRoute(input.LT(1).getText() + " " + input.LT(2).getText())))||((hasStreetNameStarted && startsUrbanNotRural)&&(Address.isRuralRoute(input.LT(1).getText()))))) ) {s = 9;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index8_1);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 8, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_streetAddressStart_in_address41 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRouteAddress_in_address47 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lockBoxAddress_in_address52 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalDeliveryAddress_in_address57 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedParse_in_address63 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_failedToken_in_failedParse79 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_set_in_failedToken93 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_streetAddress_in_streetAddressStart132 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_streetAddress160 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_streetAddress162 = new BitSet(new long[]{0x00000000000002C0L});
    public static final BitSet FOLLOW_street_in_streetAddress164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_street_in_streetAddress181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUITEANDSTREETNUM_in_street195 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street197 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NUMANDSUFFIX_in_street214 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street216 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NUMBER_in_street227 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_streetToken_in_street229 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NAME_in_streetToken259 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NUMBER_in_streetToken263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken353 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_streetToken384 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRoute_in_streetToken409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_streetToken439 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruralRoute_in_ruralRouteAddress474 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute504 = new BitSet(new long[]{0x00000000000003F0L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRoute508 = new BitSet(new long[]{0x00000000000003F0L});
    public static final BitSet FOLLOW_ruralRouteSuffix_in_ruralRoute511 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute530 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute534 = new BitSet(new long[]{0x00000000000003F0L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRoute538 = new BitSet(new long[]{0x00000000000003F0L});
    public static final BitSet FOLLOW_ruralRouteSuffix_in_ruralRoute541 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute560 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute564 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_ruralRoute568 = new BitSet(new long[]{0x00000000000003F0L});
    public static final BitSet FOLLOW_NUMBER_in_ruralRoute572 = new BitSet(new long[]{0x00000000000003F0L});
    public static final BitSet FOLLOW_ruralRouteSuffix_in_ruralRoute575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_streetAddress_in_ruralRouteSuffix598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_diTypeAndName_in_ruralRouteSuffix616 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress633 = new BitSet(new long[]{0x0000000000001080L});
    public static final BitSet FOLLOW_12_in_lockBoxAddress635 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress640 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_diTypeAndName_in_lockBoxAddress642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress661 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_lockBoxAddress665 = new BitSet(new long[]{0x0000000000001080L});
    public static final BitSet FOLLOW_12_in_lockBoxAddress667 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NUMBER_in_lockBoxAddress672 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_diTypeAndName_in_lockBoxAddress674 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress700 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_diTypeAndName_in_generalDeliveryAddress702 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress721 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress725 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_diTypeAndName_in_generalDeliveryAddress727 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress750 = new BitSet(new long[]{0x0000000000000110L});
    public static final BitSet FOLLOW_set_in_generalDeliveryAddress754 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_generalDeliveryAddress762 = new BitSet(new long[]{0x00000000000001F0L});
    public static final BitSet FOLLOW_diTypeAndName_in_generalDeliveryAddress764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName790 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName792 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName812 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName816 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName818 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName838 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName842 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_NAME_in_diTypeAndName846 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName848 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_diName_in_diTypeAndName863 = new BitSet(new long[]{0x00000000000001F2L});
    public static final BitSet FOLLOW_set_in_diName877 = new BitSet(new long[]{0x0000000000000002L});

}