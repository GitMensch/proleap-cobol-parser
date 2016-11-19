package io.proleap.cobol.gpl.parser.data.programlibrary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import io.proleap.cobol.CobolTestSupport;
import io.proleap.cobol.parser.applicationcontext.CobolParserContext;
import io.proleap.cobol.parser.metamodel.CopyBook;
import io.proleap.cobol.parser.metamodel.Program;
import io.proleap.cobol.parser.metamodel.ProgramUnit;
import io.proleap.cobol.parser.metamodel.data.DataDivision;
import io.proleap.cobol.parser.metamodel.data.programlibrary.ExportAttribute;
import io.proleap.cobol.parser.metamodel.data.programlibrary.ExportEntryProcedure;
import io.proleap.cobol.parser.metamodel.data.programlibrary.LibraryDescriptionEntry;
import io.proleap.cobol.parser.metamodel.data.programlibrary.LibraryDescriptionEntryExport;
import io.proleap.cobol.parser.metamodel.data.programlibrary.ProgramLibrarySection;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;

public class ProgramLibraryExportTest extends CobolTestSupport {

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void test() throws Exception {
		final File inputFile = new File(
				"src/test/resources/io/proleap/cobol/gpl/parser/data/programlibrary/ProgramLibraryExport.cbl");
		final Program program = CobolParserContext.getInstance().getParserRunner().analyzeFile(inputFile, null,
				CobolSourceFormatEnum.TANDEM);

		final CopyBook copyBook = program.getCopyBook("ProgramLibraryExport");
		final ProgramUnit programUnit = copyBook.getProgramUnit();
		final DataDivision dataDivision = programUnit.getDataDivision();
		final ProgramLibrarySection programLibrarySection = dataDivision.getProgramLibrarySection();

		final LibraryDescriptionEntry libraryDescriptionEntry = programLibrarySection
				.getLibraryDescriptionEntry("SOMELIB");

		assertNotNull(libraryDescriptionEntry);
		assertEquals(LibraryDescriptionEntry.Type.Export, libraryDescriptionEntry.getType());

		final LibraryDescriptionEntryExport libraryDescriptionEntryExport = (LibraryDescriptionEntryExport) libraryDescriptionEntry;

		{
			final ExportAttribute exportAttribute = libraryDescriptionEntryExport.getExportAttribute();
			assertNotNull(exportAttribute);
			assertEquals(ExportAttribute.Sharing.Private, exportAttribute.getSharing());
		}

		{
			final ExportEntryProcedure exportEntryProcedure = libraryDescriptionEntryExport.getExportEntryProcedure();
			assertNotNull(exportEntryProcedure);
			assertNotNull(exportEntryProcedure.getProgramValueStmt());
			assertNotNull(exportEntryProcedure.getForClause());
			assertEquals("123", exportEntryProcedure.getForClause().getForLiteral().getValue());
		}
	}
}