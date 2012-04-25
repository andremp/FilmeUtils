package filmeUtils.http;

import java.util.Arrays;

/**
 * <b>Bare Bones Browser Launch for Java</b><br>
 * Utility class to open a web page from a Swing application in the user's
 * default browser.<br>
 * Supports: Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7<br>
 * Example Usage:<code><br> &nbsp; &nbsp;
 *    String url = "http://www.google.com/";<br> &nbsp; &nbsp;
 *    BareBonesBrowserLaunch.openURL(url);<br></code> Latest Version: <a
 * href="http://www.centerkey.com/java/browser/"
 * >www.centerkey.com/java/browser</a><br>
 * Author: Dem Pilafian<br>
 * Public Domain Software -- Free to Use as You Like
 * 
 * @version 3.1, June 6, 2010
 */
public class BareBonesBrowseLauncher implements BrowserLauncher {

	static final String[] browsers = { "firefox", "google-chrome", "opera",
			"epiphany", "konqueror", "conkeror", "midori", "kazehakase",
			"mozilla" };
	private final String osName;

	public BareBonesBrowseLauncher() {
		osName = System.getProperty("os.name");
	}
	
	/* (non-Javadoc)
	 * @see filmeUtils.BrowserLauncher#openURL(java.lang.String)
	 */
	public void openURL(final String url) {
		try { // attempt to use Desktop library from JDK 1.6+
			final Class<?> d = Class.forName("java.awt.Desktop");
			d.getDeclaredMethod("browse", new Class[] {java.net.URI.class }).invoke(d.getDeclaredMethod("getDesktop").invoke(null),new Object[] { java.net.URI.create(url) });
			// above code mimicks: java.awt.Desktop.getDesktop().browse()
		} catch (final Exception ignore) { // library not available or failed
			try {
				if (osName.startsWith("Mac OS")) {
					Class.forName("com.apple.eio.FileManager").getDeclaredMethod("openURL",new Class[] { String.class }).invoke(null, new Object[] { url });
				} else if (osName.startsWith("Windows")){
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				} else { // assume Unix or Linux
					String browser = null;
					for (final String b : browsers){
						if (browser == null && Runtime.getRuntime().exec(new String[] { "which", b }).getInputStream().read() != -1){
							Runtime.getRuntime().exec(new String[] { browser = b, url });
						}
					}
					if (browser == null){
						throw new Exception(Arrays.toString(browsers));
					}
				}
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}