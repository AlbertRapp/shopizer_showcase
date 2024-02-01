package com.salesmanager.test.content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.content.ContentService;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.InputContentFile;
import com.salesmanager.core.model.content.OutputContentFile;
import com.salesmanager.core.model.merchant.MerchantStore;

/**
 * Test content with CMS store logo
 * 
 * @author Carl Samson
 *
 */
@Ignore
public class ContentImagesTest extends com.salesmanager.test.common.AbstractSalesManagerCoreTestCase {

	@Inject
	private ContentService contentService;

	// @Test
	@Ignore
	public void createStoreLogo() throws ServiceException, FileNotFoundException, IOException {

		MerchantStore store = merchantService.getByCode(MerchantStore.DEFAULT_STORE);





/**********************************
 * CAST-Finding START #1 (2024-02-01 21:58:35.688018):
 * TITLE: Use a virtualised environment where possible
 * DESCRIPTION: Footprint measurements clearly show that a virtual server is ten times more energy efficient than a physical server. The superfluous capacity of the server can be used by other applications. When creating the architecture of an application, bear in mind that all parts will be virtualized.  Cloud infrastructures comply with the ISO 50001 standard, which respects energy sobriety. Also "Cloudify" resources offers resource pooling.
 * OUTLINE: The code line `MerchantStore store = merchantService.getByCode(MerchantStore.DEFAULT_STORE);` is most likely affected.  - Reasoning: The method call `getByCode` could potentially involve resource usage, such as database queries or network requests.  - Proposed solution: Cache the result of the method call if it is used multiple times within the same context.
 * INSTRUCTION: Please follow the OUTLINE and conduct the proposed steps with the affected code.
 * STATUS: REVIEWED
 * CAST-Finding END #1
 **********************************/


		final File file1 = new File("C:/doc/Hadoop.jpg");

		if (!file1.exists() || !file1.canRead()) {
			throw new ServiceException("Can't read" + file1.getAbsolutePath());
		}



/**********************************
 * CAST-Finding START #2 (2024-02-01 21:58:35.688018):
 * TITLE: Avoid Programs not using explicitly OPEN and CLOSE for files or streams
 * DESCRIPTION: Not closing files explicitly into your programs can occur memory issues. Leaving files opened unnecessarily has many downsides. They may consume limited system resources such as file descriptors. Code that deals with many such objects may exhaust those resources unnecessarily if they're not returned to the system promptly after use.
 * OUTLINE: The code line `final File file1 = new File("C:/doc/Hadoop.jpg");` is most likely affected.  - Reasoning: It creates a `File` object without explicitly closing it.  - Proposed solution: Add a `try-finally` block to ensure that the file is closed after it is no longer needed.  The code line `if (!file1.exists() || !file1.canRead()) {` is most likely affected.  - Reasoning: It checks if the file exists and is readable without explicitly closing it.  - Proposed solution: Add a `try-finally` block to ensure that the file is closed after it is no longer needed.  The code line `throw new ServiceException("Can't read" + file1.getAbsolutePath());` is most likely affected.  - Reasoning: It throws an exception without explicitly closing the file.  - Proposed solution: Add a `try-finally` block to ensure that the file is closed after it is no longer needed.  The code line `byte[] is = IOUtils.toByteArray(new FileInputStream(file1));` is most likely affected.  - Reasoning: It reads the file without explicitly closing it.  - Proposed solution: Add a `try-finally` block to ensure that the file input stream is closed after it is no longer needed.  The code line `ByteArrayInputStream inputStream = new ByteArrayInputStream(is);` is most likely affected.  - Reasoning: It creates a `ByteArrayInputStream` without explicitly closing it.  - Proposed solution: Add a `try-finally` block to ensure that the input stream is closed after it is no longer needed.  The code line `InputContentFile cmsContentImage = new InputContentFile();` is most likely affected.  - Reasoning: It creates an `InputContentFile` object without explicitly closing it.  - Proposed solution: Add a `try-finally` block to ensure that the object is closed after it is no longer needed.  The code line `cmsContentImage.setFileName(file1.getName
 * INSTRUCTION: Please follow the OUTLINE and conduct the proposed steps with the affected code.
 * STATUS: REVIEWED
 * CAST-Finding END #2
 **********************************/
 **********************************/
 **********************************/


		byte[] is = IOUtils.toByteArray(new FileInputStream(file1));
		ByteArrayInputStream inputStream = new ByteArrayInputStream(is);
		InputContentFile cmsContentImage = new InputContentFile();

		cmsContentImage.setFileName(file1.getName());
		cmsContentImage.setFile(inputStream);

		// logo as a content
		contentService.addLogo(store.getCode(), cmsContentImage);

		store.setStoreLogo(file1.getName());
		merchantService.update(store);

		// query the store
		store = merchantService.getByCode(MerchantStore.DEFAULT_STORE);

		// get the logo
		String logo = store.getStoreLogo();

		OutputContentFile image = contentService.getContentFile(store.getCode(), FileContentType.LOGO, logo);

		// print image
/**********************************
 * CAST-Finding START #3 (2024-02-01 21:58:35.688018):
 * TITLE: Use a virtualised environment where possible
 * DESCRIPTION: Footprint measurements clearly show that a virtual server is ten times more energy efficient than a physical server. The superfluous capacity of the server can be used by other applications. When creating the architecture of an application, bear in mind that all parts will be virtualized.  Cloud infrastructures comply with the ISO 50001 standard, which respects energy sobriety. Also "Cloudify" resources offers resource pooling.
 * OUTLINE: The code line `store = merchantService.getByCode(MerchantStore.DEFAULT_STORE);` is most likely affected. - Reasoning: The method `getByCode` in the `merchantService` may need to be updated to align with the recommended practices mentioned in the finding. - Proposed solution: Update the `getByCode` method in the `merchantService` to ensure it follows the recommended practices mentioned in the finding.
 * INSTRUCTION: Please follow the OUTLINE and conduct the proposed steps with the affected code.
 * STATUS: REVIEWED
 * CAST-Finding END #3
 **********************************/
 * CAST-Finding END #3
 **********************************/
 * CAST-Finding END #3
 **********************************/
/**********************************
 * CAST-Finding START #4 (2024-02-01 21:58:35.688018):
 * TITLE: Avoid Programs not using explicitly OPEN and CLOSE for files or streams
 * DESCRIPTION: Not closing files explicitly into your programs can occur memory issues. Leaving files opened unnecessarily has many downsides. They may consume limited system resources such as file descriptors. Code that deals with many such objects may exhaust those resources unnecessarily if they're not returned to the system promptly after use.
 * OUTLINE: The code line `OutputStream outputStream = new FileOutputStream("C:/doc/logo-" + image.getFileName());` is most likely affected. - Reasoning: It opens a file without explicitly closing it, which can lead to memory issues and resource exhaustion. - Proposed solution: Add a `try-finally` block to ensure that the `outputStream` is closed after it is used.
 * INSTRUCTION: Please follow the OUTLINE and conduct the proposed steps with the affected code.
 * STATUS: REVIEWED
 * CAST-Finding END #4
 **********************************/
 * STATUS: IN_PROGRESS
 * CAST-Finding END #4
 **********************************/
 * STATUS: OPEN
 * CAST-Finding END #4
 **********************************/


		OutputStream outputStream = new FileOutputStream("C:/doc/logo-" + image.getFileName());

		ByteArrayOutputStream baos = image.getFile();
		baos.writeTo(outputStream);

		// remove image
		contentService.removeFile(store.getCode(), FileContentType.LOGO, store.getStoreLogo());

	}
	

}