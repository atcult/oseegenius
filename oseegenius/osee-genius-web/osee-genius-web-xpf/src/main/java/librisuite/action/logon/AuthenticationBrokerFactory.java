package librisuite.action.logon;

import librisuite.business.authentication.AuthenticationBroker;
import librisuite.business.colgate.Colgate;

/**
 * Factory to switch between different authentication methods
 *  
 * @author Mirko Fonzo
 *
 * Ver. 1.1
 */
public class AuthenticationBrokerFactory {

	public static final int COLGATE = 1;
	public static final int LDAP    = 2;

//	public static AuthenticationBroker getAuthenticationBroker(int authMethod) {
//		switch (authMethod) {
//		case COLGATE:
//			return new Colgate();
//		case LDAP:
//			return new LdapAuthentication();
//		// add here further authentication methods
//		default:
//			return new Colgate();
//		}
//	}
	
	//MAURA
	public static AuthenticationBroker getAuthenticationBroker(int authMethod) {
		
			return new Colgate();
	}

//	public static AuthenticationBroker getDefaultAuthenticationBroker() {
//		return new LdapAuthentication();
//	}	
	
}
