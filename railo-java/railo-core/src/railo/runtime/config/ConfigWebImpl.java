

package railo.runtime.config;

import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import railo.commons.io.res.Resource;
import railo.commons.io.res.ResourceProvider;
import railo.commons.io.res.ResourcesImpl;
import railo.runtime.CFMLFactory;
import railo.runtime.Page;
import railo.runtime.cfx.CFXTagPool;
import railo.runtime.compiler.CFMLCompilerImpl;
import railo.runtime.exp.ExpressionException;
import railo.runtime.exp.PageException;
import railo.runtime.exp.SecurityException;
import railo.runtime.lock.LockManager;
import railo.runtime.lock.LockManagerImpl;
import railo.runtime.security.SecurityManager;
import railo.runtime.security.SecurityManagerImpl;

/**
 * Web Context
 */
public final class ConfigWebImpl extends ConfigImpl implements ServletConfig, ConfigWeb {
    
    private ServletConfig config;
    private ConfigServerImpl configServer;
    private SecurityManager securityManager;
    private LockManager lockManager= LockManagerImpl.getInstance();
    private Resource rootDir;
    private CFMLCompilerImpl compiler=new CFMLCompilerImpl();
    private Page baseComponentPage;

    //private File deployDirectory;

    /**
     * constructor of the class
     * @param configServer
     * @param config
     * @param configDir
     * @param configFile
     */
    protected ConfigWebImpl(CFMLFactory factory,ConfigServerImpl configServer, ServletConfig config, Resource configDir, Resource configFile) {
    	super(factory,configDir, configFile,configServer.getTLDs(),configServer.getFLDs());
    	//super(configDir, configFile, config.getServletContext().getRealPath("/"));
        this.configServer=configServer;
        this.config=config;
        ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
        this.rootDir=frp.getResource(config.getServletContext().getRealPath("/"));
        
    }

    /**
     * @see javax.servlet.ServletConfig#getServletName()
     */
    public String getServletName() {
        return config.getServletName();
    }

    /**
     * @see javax.servlet.ServletConfig#getServletContext()
     */
    public ServletContext getServletContext() {
        return config.getServletContext();
    }

    /**
     * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String name) {
        return config.getInitParameter(name);
    }

    /**
     * @see javax.servlet.ServletConfig#getInitParameterNames()
     */
    public Enumeration getInitParameterNames() {
        return config.getInitParameterNames();
    }

    /**
     * @see railo.runtime.config.ConfigImpl#getConfigServerImpl()
     */
    public ConfigServerImpl getConfigServerImpl() {
        return configServer;
    }
    

    public ConfigServer getConfigServer() {
        return configServer;
    }
    
    /**
     * @see railo.runtime.config.ConfigImpl#getConfigServer(java.lang.String)
     */
    public ConfigServer getConfigServer(String password) throws ExpressionException {
        if(!configServer.hasPassword())
            throw new ExpressionException("can't access, no password is defined");
        if(!configServer.getPassword().equalsIgnoreCase(password))
            throw new ExpressionException("no acccess, password is invalid");
        return configServer;
    }
    
    // FUTURE
    public String getServerId() {
        return configServer.getId();
    }

    public String getServerSecurityKey() {
        return configServer.getSecurityKey();
    }
    
    public Resource getServerConfigDir() {
        return configServer.getConfigDir();
    }
    

    /**
     * @return Returns the accessor.
     */
    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * @param securityManager The accessor to set.
     */
    protected void setSecurityManager(SecurityManager securityManager) {
        ((SecurityManagerImpl)securityManager).setConfig(rootDir);
        this.securityManager = securityManager;
    }
    
    /**
     * @throws SecurityException 
     * @see railo.runtime.config.ConfigImpl#getCFXTagPool()
     */
    public CFXTagPool getCFXTagPool() throws SecurityException {
        if(securityManager.getAccess(SecurityManager.TYPE_CFX_USAGE)==SecurityManager.VALUE_YES) return super.getCFXTagPool();
        throw new SecurityException("no access to cfx functionality", "disabled by security settings");
    }

    /**
     * @return Returns the rootDir.
     */
    public Resource getRootDirectory() {
        return rootDir;
    }

    /**
     * @see railo.runtime.config.Config#getUpdateType()
     */
    public String getUpdateType() {
        return configServer.getUpdateType();
    }

    /**
     * @see railo.runtime.config.Config#getUpdateLocation()
     */
    public URL getUpdateLocation() {
        return configServer.getUpdateLocation();
    }

    /**
     * @see railo.runtime.config.ConfigWeb#getLockManager()
     */
    public LockManager getLockManager() {
        return lockManager;
    }

	/**
	 * @return the compiler
	 */
	public CFMLCompilerImpl getCompiler() {
		return compiler;
	}
	
	 public Page getBaseComponentPage() throws PageException {
	        if(baseComponentPage==null) {
	            baseComponentPage=getBaseComponentPageSource().loadPage(this);
				
	        }
	        return baseComponentPage;
	    }
	    public void resetBaseComponentPage() {
	        baseComponentPage=null;
	    }

}