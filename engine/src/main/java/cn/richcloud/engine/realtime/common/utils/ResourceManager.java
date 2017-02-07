package cn.richcloud.engine.realtime.common.utils;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 */

public class ResourceManager {

    private final ResourceBundle bundle;
    private final Locale locale;

    private ResourceManager(String resourcePath, Locale locale) {
        String bundleResourcePath = resourcePath;
        ResourceBundle bnd = null;
        try {
            bnd = ResourceBundle.getBundle(bundleResourcePath, locale);
        } catch( MissingResourceException ex ) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if( cl != null ) {
                try {
                    bnd = ResourceBundle.getBundle(bundleResourcePath, locale, cl);
                } catch(MissingResourceException ex2) {
                    // Ignore
                }
            }
        }
        bundle = bnd;
        // Get the actual locale, which may be different from the requested one
        if (bundle != null) {
            this.locale = bundle.getLocale();
        } else {
            this.locale = null;
        }
    }

    /**
        Get a string from the underlying resource bundle or return
        null if the String is not found.

        @param key to desired resource String
        @return resource String matching <i>key</i> from underlying
                bundle or null if not found.
        @throws IllegalArgumentException if <i>key</i> is null.
     */
    public String getString(String key) {
        if(key == null){
            String msg = "key may not have a null value";

            throw new IllegalArgumentException(msg);
        }

        String str = null;

        try {
            // Avoid NPE if bundle is null and treat it like an MRE
            if (bundle != null) {
                str = bundle.getString(key);
            }
        } catch(MissingResourceException mre) {
            str = null;
        }

        return str;
    }
    
    
    public int getInteger(String key){
    	String ret = getString(key);
    	return Integer.valueOf(ret.trim());
    }

    /**
     * Get a string from the underlying resource bundle and format
     * it with the given set of arguments.
     *
     * @param key
     * @param args
     */
    public String getString(final String key, final Object... args) {
        String value = getString(key);
        if (value == null) {
            value = key;
        }

        MessageFormat mf = new MessageFormat(value);
        mf.setLocale(locale);
        return mf.format(args, new StringBuffer(), null).toString();
    }

    /**
     * Identify the Locale this StringManager is associated with
     */
    public Locale getLocale() {
        return locale;
    }

    // --------------------------------------------------------------
    // STATIC SUPPORT METHODS
    // --------------------------------------------------------------

    private static final Map<String, Map<Locale,ResourceManager>> managers =
        new Hashtable<String, Map<Locale,ResourceManager>>();

    /**
     * @param resourcePath The package name
     */
    public static final synchronized ResourceManager getManager(
            String resourcePath) {
        return getManager(resourcePath, Locale.getDefault());
    }

    /**
     * @param resourcePath The package name
     * @param locale      The Locale
     */
    public static final synchronized ResourceManager getManager(
            String resourcePath, Locale locale) {

        Map<Locale,ResourceManager> map = managers.get(resourcePath);
        if (map == null) {
            map = new Hashtable<Locale, ResourceManager>();
            managers.put(resourcePath, map);
        }

        ResourceManager mgr = map.get(locale);
        if (mgr == null) {
            mgr = new ResourceManager(resourcePath, locale);
            map.put(locale, mgr);
        }
        return mgr;
    }
}
