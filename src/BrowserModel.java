import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * This represents the heart of the browser: the collections
 * that organize all the URLs into useful structures.
 * 
 * @author Robert C. Duvall
 */
public class BrowserModel {
    // constants
    public static final String PROTOCOL_PREFIX = "http://";
    // state
    private URL myHome;
    private URL myCurrentURL;
    private int myCurrentIndex;
    private List<URL> myHistory;
    private Map<String, URL> myFavorites;
    private ResourceBundle myResources;
    private String DEFAULT_RESOURCE_PACKAGE = "errorResources/";


    /**
     * Creates an empty model.
     */
    public BrowserModel () {
        myHome = null;
        myCurrentURL = null;
        myCurrentIndex = -1;
        myHistory = new ArrayList<>();
        myFavorites = new HashMap<>();
        myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE + "errorMessage");
    }

    /**
     * Returns the first page in next history, null if next history is empty.
     */
    public URL next () {
        try {
            myCurrentIndex++;
            return myHistory.get(myCurrentIndex);
        }
        catch(BrowserException e) {
        	throw new BrowserException(String.format(myResources.getString("ForwardError")));
        }
    }

    /**
     * Returns the first page in back history, null if back history is empty.
     */
    public URL back() {
        try {
            myCurrentIndex--;
            return myHistory.get(myCurrentIndex);
        }
        catch (BrowserException e) {
        	throw new BrowserException(String.format(myResources.getString("BackError")));
        }
    }

    /**
     * Changes current page to given URL, removing next history.
     */
    public URL go (String url) {
        try {
            URL tmp = completeURL(url);
            // unfortunately, completeURL may not have returned a valid URL, so test it
            tmp.openStream();
            // if successful, remember this URL
            myCurrentURL = tmp;
            //if (myCurrentURL != null) {
                if (hasNext()) {
                    myHistory = myHistory.subList(0, myCurrentIndex + 1);
                }
                myHistory.add(myCurrentURL);
                myCurrentIndex++;
            //}
            return myCurrentURL;
        }
        catch (Exception e) { 
        	throw new BrowserException(String.format(myResources.getString("BrowserError"), url));
        }
    }

    /**
     * Returns true if there is a next URL available
     */
    public boolean hasNext () {
        return myCurrentIndex < (myHistory.size() - 1);
    }

    /**
     * Returns true if there is a previous URL available
     */
    public boolean hasPrevious () {
        return myCurrentIndex > 0;
    }

    /**
     * Returns URL of the current home page or null if none is set.
     */
    public URL getHome () {
        return myHome;
    }

    /**
     * Sets current home page to the current URL being viewed.
     */
    public void setHome () {
        // just in case, might be called before a page is visited
        try {
            myHome = myCurrentURL;
        }
        catch (BrowserException e) {
        	throw new BrowserException(String.format(myResources.getString("HomeError")));
        }
    }

    /**
     * Adds current URL being viewed to favorites collection with given name.
     */
    public void addFavorite (String name) {
        // just in case, might be called before a page is visited
       try {
    	   if(!name.equals("") && myCurrentURL != null) {
    		   myFavorites.put(name, myCurrentURL);
    	   }
        }
       catch (BrowserException e) {
    	   throw new BrowserException(String.format(myResources.getString("NoNameError")));
       }
    }

    /**
     * Returns URL from favorites associated with given name, null if none set.
     */
    public URL getFavorite (String name) {
        try {
            return myFavorites.get(name);
        }
        catch(BrowserException e) {
        	throw new BrowserException(String.format(myResources.getString("NoFavoriteError")));
        }
    }

    // deal with a potentially incomplete URL
    private URL completeURL (String possible) {
        try {
            // try it as is
            return new URL(possible);
        } catch (MalformedURLException e) {
            try {
                // try it as a relative link
                // BUGBUG: need to generalize this :(
                return new URL(myCurrentURL.toString() + "/" + possible);
            } catch (MalformedURLException ee) {
                try {
                    // e.g., let user leave off initial protocol
                    return new URL(PROTOCOL_PREFIX + possible);
                } catch (MalformedURLException eee) {
                    return null;
                }
            }
        }
    }
}
