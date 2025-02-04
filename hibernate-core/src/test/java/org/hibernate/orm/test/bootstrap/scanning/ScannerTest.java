/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.bootstrap.scanning;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.hibernate.boot.archive.scan.internal.StandardScanOptions;
import org.hibernate.boot.archive.scan.internal.StandardScanParameters;
import org.hibernate.boot.archive.scan.internal.StandardScanner;
import org.hibernate.boot.archive.scan.spi.ClassDescriptor;
import org.hibernate.boot.archive.scan.spi.MappingFileDescriptor;
import org.hibernate.boot.archive.scan.spi.ScanEnvironment;
import org.hibernate.boot.archive.scan.spi.ScanOptions;
import org.hibernate.boot.archive.scan.spi.ScanResult;
import org.hibernate.boot.archive.scan.spi.Scanner;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.StandardJpaScanEnvironmentImpl;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.orm.test.jpa.pack.defaultpar.ApplicationServer;
import org.hibernate.orm.test.jpa.pack.defaultpar.Version;

import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ScannerTest extends PackagingTestCase {
	@Test
	public void testNativeScanner() throws Exception {
		File defaultPar = buildDefaultPar();
		addPackageToClasspath( defaultPar );

		PersistenceUnitDescriptor descriptor = new ParsedPersistenceXmlDescriptor( defaultPar.toURL() );
		ScanEnvironment env = new StandardJpaScanEnvironmentImpl( descriptor );
		ScanOptions options = new StandardScanOptions( "hbm,class", descriptor.isExcludeUnlistedClasses() );
		Scanner scanner = new StandardScanner();
		ScanResult scanResult = scanner.scan(
				env,
				options,
				StandardScanParameters.INSTANCE
		);

		assertEquals( 3, scanResult.getLocatedClasses().size() );
		assertClassesContained( scanResult, ApplicationServer.class );
		assertClassesContained( scanResult, Version.class );

		assertEquals( 2, scanResult.getLocatedMappingFiles().size() );
		for ( MappingFileDescriptor mappingFileDescriptor : scanResult.getLocatedMappingFiles() ) {
			assertNotNull( mappingFileDescriptor.getName() );
			assertNotNull( mappingFileDescriptor.getStreamAccess() );
			InputStream stream = mappingFileDescriptor.getStreamAccess().accessInputStream();
			assertNotNull( stream );
			stream.close();
		}
	}

	private void assertClassesContained(ScanResult scanResult, Class classToCheckFor) {
		for ( ClassDescriptor classDescriptor : scanResult.getLocatedClasses() ) {
			if ( classDescriptor.getName().equals( classToCheckFor.getName() ) ) {
				return;
			}
		}
		fail( "ScanResult did not contain expected Class : " + classToCheckFor.getName() );
	}

	@Test
	public void testCustomScanner() throws Exception {
		File defaultPar = buildDefaultPar();
		File explicitPar = buildExplicitPar();
		addPackageToClasspath( defaultPar, explicitPar );
		
		EntityManagerFactory emf;
		CustomScanner.resetUsed();
		final Map<String, Object> integration = ServiceRegistryUtil.createBaseSettings();
		emf = Persistence.createEntityManagerFactory( "defaultpar", integration );
		assertTrue( ! CustomScanner.isUsed() );
		emf.close();

		CustomScanner.resetUsed();
		emf = Persistence.createEntityManagerFactory( "manager1", integration );
		assertTrue( CustomScanner.isUsed() );
		emf.close();

		CustomScanner.resetUsed();
		integration.put( AvailableSettings.SCANNER, new CustomScanner() );
		emf = Persistence.createEntityManagerFactory( "defaultpar", integration );
		assertTrue( CustomScanner.isUsed() );
		emf.close();

		CustomScanner.resetUsed();
		emf = Persistence.createEntityManagerFactory( "defaultpar", ServiceRegistryUtil.createBaseSettings() );
		assertTrue( ! CustomScanner.isUsed() );
		emf.close();
	}
}
