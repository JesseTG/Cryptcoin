package jtg.cse260.cryptcoin;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class Resources {
    private static Map<String, Icon> icons;
    
    static {
        icons = new HashMap<String, Icon>();
    }
    
    /**
     * Loads an icon from the file system, given its ID. Do not add the file
     * extension. Does not return null; if the requested icon isn't found,
     * return the empty icon.
     * 
     * @param name The name of the icon.
     * @return The icon
     */
    public static Icon getIcon(final String name) {
        if (!icons.containsKey(name)) {
            icons.put(name, new ImageIcon(String.format("icons/%s.png", name)));
        }
            
        return icons.get(name);
    }

    private Resources() {

    }
}
