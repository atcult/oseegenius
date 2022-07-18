package librisuite.business.authorisation;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import librisuite.business.authentication.DAOUserAccount;
import librisuite.business.common.DataAccessException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MadesAuthority extends AmicusAuthority {
	private static final Log logger = LogFactory.getLog(MadesAuthority.class);

	private int madesAuthorisationLevel = -1;

	public MadesAuthority(String userAccount) throws DataAccessException, AuthorisationException {
		super(userAccount);
		DAOUserAccount dao = new DAOUserAccount();
		List l = dao.getModuleAuthorisations(userAccount);
		Iterator iter = l.iterator();
		ModuleAuthorisation anAuth;
		while (iter.hasNext()) {
			anAuth = (ModuleAuthorisation) iter.next();
			if ("mad".equals(anAuth.getModuleCode())) {
				madesAuthorisationLevel = anAuth.getAuthorisationLevel();
			}
		}
		if(madesAuthorisationLevel==-1) {
			madesAuthorisationLevel = 1; // TODO _MIKE: [HI] per Default livello di autorizzazione minimo
			logger.error("User "+userAccount+" is not able to use MADES");
			// TODO _MIKE: [HI] commentare per disabilitare i controlli sui permessi
			throw new AuthorisationException("User "+userAccount+" is not able to use MADES");
		}
	}

	/**
	 * @return Returns the madesAuthorisationLevel.
	 */
	public int getMadesAuthorisationLevel() {
		return madesAuthorisationLevel;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = getClass().getName()
		+ " madesAuthorisationLevel: "+madesAuthorisationLevel
		+ " permissions: ";
		Set/*<Permission>*/ permissions = getPermissions();
		Iterator it = permissions.iterator();
		while(it.hasNext()){
			Permission p = (Permission) it.next();
			s += p.getName() + " ";
		}
		return s;
	}

	public void opacaMode() {
		getTagPermissions().clear();
		removePermission("basicCataloguing");
		removePermission("madReport");
		removePermission("advancedCataloguing");
		removePermission("moveHierarchy");
		removePermission("usePredefined");
		removePermission("editCopies");
		removePermission("editHeadings");
		removePermission("editXRefs");
		removePermission("editAuthority");
		removePermission("showAuthority");
	}
}
