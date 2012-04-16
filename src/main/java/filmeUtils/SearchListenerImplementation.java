package filmeUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionPoolTimeoutException;

import com.github.junrar.testutil.ExtractArchive;

import filmeUtils.torrentSites.PirateBaySe;

final class SearchListenerImplementation implements SearchListener {
	private final FilmeUtilsHttpClient httpclient;
	private final boolean extractContents;
	private final PirateBaySe pirateBaySe;
	private final File subtitleDestination;
	private final boolean showDirectLink;
	private final boolean showSubtitleIfMagnetWasNotFound;

	SearchListenerImplementation(final FilmeUtilsHttpClient httpclient,final boolean showDirectLink,final boolean showSubtitleIfMagnetWasNotFound, final File subtitleDestination) {
		this.httpclient = httpclient;
		this.showSubtitleIfMagnetWasNotFound = showSubtitleIfMagnetWasNotFound;
		this.extractContents = subtitleDestination!= null;
		this.showDirectLink = showDirectLink;
		this.subtitleDestination = subtitleDestination;
        pirateBaySe = new PirateBaySe(httpclient);
	}

	public void found(final String name, final String link) {
		String direct_link = "";
		if(showDirectLink){
			direct_link = " - "+link;
		}
		System.out.println(name+direct_link);
		if(!extractContents){
			return;
		}
		unzipAndPrint(name,link);
	}

	private void unzipAndPrint(final String name,final String link){
		try {
			final String validNameForFile = name.replaceAll("[/ \\\\?]", "_");
			final File currentSubtitleCollection = new File(subtitleDestination, validNameForFile);
			currentSubtitleCollection.mkdir();
			System.out.println("Extraindo legendas para "+currentSubtitleCollection.getAbsolutePath());
			final File destFile = new File(currentSubtitleCollection,"compressedSubs");
			destFile.createNewFile();
			
	    	final HttpGet httpGet = new HttpGet(link);
	    	final String contentType = httpclient.executeSaveResponseToFileReturnContentType(httpGet, destFile);
			
			if(contentType.contains("rar")){
				ExtractArchive.extractArchive(destFile, currentSubtitleCollection);
			}else{
				final ZipFile zipFile = new ZipFile(destFile);
				
				final Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while(entries.hasMoreElements()) {
					final ZipEntry entry = entries.nextElement();
					final File unzippingFile = new File(currentSubtitleCollection,entry.getName());
					if(entry.isDirectory()) {
						unzippingFile.mkdir();
					}else{
						unzippingFile.createNewFile();
						final FileOutputStream fileOutputStream = new FileOutputStream(unzippingFile);
						final InputStream inputStream = zipFile.getInputStream(entry);
						IOUtils.copy(inputStream, fileOutputStream);
						inputStream.close();
						fileOutputStream.close();
					}
				}
				zipFile.close();
				destFile.delete();
			}
			
			@SuppressWarnings("unchecked")
			final Iterator<File> iterateFiles = FileUtils.iterateFiles(currentSubtitleCollection, new String[]{"srt"}, true);
			while(iterateFiles.hasNext()){
				final File next = iterateFiles.next();
				showFileName(next);
			}
			
		} catch(final ConnectionPoolTimeoutException e){
			System.out.println("Tempo máximo de requisição atingido ("+FilmeUtilsHttpClient.TIMEOUT+" segundos)");
		}catch (final IOException e1) {
			throw new RuntimeException(e1);
		}
	}

	private void showFileName(final File next) {
		String magnetLinkForFile;
		final String subtitleName = next.getName().replaceAll("\\.[Ss][Rr][Tt]", "");
		String subtitleNameFormmated = "\t* "+subtitleName;
		magnetLinkForFile = pirateBaySe.getMagnetLinkForFileOrNull(subtitleName);
		if(magnetLinkForFile == null){
			if(showSubtitleIfMagnetWasNotFound){
				System.out.println(subtitleNameFormmated + " - magnet não foi encontrado");
			}
			return;
		}
		System.out.println(subtitleNameFormmated + " - " + magnetLinkForFile);
	}
}