/*
 * Copyright (C) 2016, Ulrich Wolffgang <u.wol@wwu.de>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the BSD 3-clause license. See the LICENSE file for details.
 */

package io.proleap.cobol.asg.metamodel.data.datadescription.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.proleap.cobol.Cobol85Parser.DataAlignedClauseContext;
import io.proleap.cobol.Cobol85Parser.DataBlankWhenZeroClauseContext;
import io.proleap.cobol.Cobol85Parser.DataCommonOwnLocalClauseContext;
import io.proleap.cobol.Cobol85Parser.DataDescriptionEntryContext;
import io.proleap.cobol.Cobol85Parser.DataDescriptionEntryExecSqlContext;
import io.proleap.cobol.Cobol85Parser.DataDescriptionEntryFormat1Context;
import io.proleap.cobol.Cobol85Parser.DataDescriptionEntryFormat2Context;
import io.proleap.cobol.Cobol85Parser.DataDescriptionEntryFormat3Context;
import io.proleap.cobol.Cobol85Parser.DataExternalClauseContext;
import io.proleap.cobol.Cobol85Parser.DataGlobalClauseContext;
import io.proleap.cobol.Cobol85Parser.DataIntegerStringClauseContext;
import io.proleap.cobol.Cobol85Parser.DataJustifiedClauseContext;
import io.proleap.cobol.Cobol85Parser.DataOccursClauseContext;
import io.proleap.cobol.Cobol85Parser.DataPictureClauseContext;
import io.proleap.cobol.Cobol85Parser.DataReceivedByClauseContext;
import io.proleap.cobol.Cobol85Parser.DataRecordAreaClauseContext;
import io.proleap.cobol.Cobol85Parser.DataRedefinesClauseContext;
import io.proleap.cobol.Cobol85Parser.DataSignClauseContext;
import io.proleap.cobol.Cobol85Parser.DataSynchronizedClauseContext;
import io.proleap.cobol.Cobol85Parser.DataThreadLocalClauseContext;
import io.proleap.cobol.Cobol85Parser.DataTypeClauseContext;
import io.proleap.cobol.Cobol85Parser.DataTypeDefClauseContext;
import io.proleap.cobol.Cobol85Parser.DataUsageClauseContext;
import io.proleap.cobol.Cobol85Parser.DataUsingClauseContext;
import io.proleap.cobol.Cobol85Parser.DataValueClauseContext;
import io.proleap.cobol.Cobol85Parser.DataWithLowerBoundsClauseContext;
import io.proleap.cobol.asg.metamodel.ProgramUnit;
import io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntry;
import io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntryCondition;
import io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntryContainer;
import io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntryExecSql;
import io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntryGroup;
import io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntryRename;
import io.proleap.cobol.asg.metamodel.impl.CobolDivisionElementImpl;
import io.proleap.cobol.asg.util.StringUtils;
import io.proleap.cobol.asg.util.TagUtils;
import io.proleap.cobol.preprocessor.CobolPreprocessor;

public abstract class DataDescriptionEntryContainerImpl extends CobolDivisionElementImpl
		implements DataDescriptionEntryContainer {

	private final static Logger LOG = LogManager.getLogger(DataDescriptionEntryContainerImpl.class);

	protected List<DataDescriptionEntry> dataDescriptionEntries = new ArrayList<DataDescriptionEntry>();

	protected Map<String, DataDescriptionEntry> dataDescriptionEntriesSymbolTable = new HashMap<String, DataDescriptionEntry>();

	public DataDescriptionEntryContainerImpl(final ProgramUnit programUnit, final ParserRuleContext ctx) {
		super(programUnit, ctx);
	}

	@Override
	public DataDescriptionEntryCondition addDataDescriptionEntryCondition(
			final DataDescriptionEntryFormat3Context ctx) {
		DataDescriptionEntryCondition result = (DataDescriptionEntryCondition) getASGElement(ctx);

		if (result == null) {
			final String name = determineName(ctx);
			result = new DataDescriptionEntryConditionImpl(name, this, programUnit, ctx);

			result.setLevelNumber(DataDescriptionEntry.LEVEL_NUMBER_CONDITION);
			result.addValueClause(ctx.dataValueClause());

			registerASGElement(result);

			dataDescriptionEntries.add(result);
			dataDescriptionEntriesSymbolTable.put(name, result);
		}

		return result;
	}

	@Override
	public DataDescriptionEntryExecSql addDataDescriptionEntryExecSql(final DataDescriptionEntryExecSqlContext ctx) {
		DataDescriptionEntryExecSql result = (DataDescriptionEntryExecSql) getASGElement(ctx);

		if (result == null) {
			result = new DataDescriptionEntryExecSqlImpl(this, programUnit, ctx);

			final String execSqlText = TagUtils.getUntaggedText(ctx.EXECSQLLINE(), CobolPreprocessor.EXEC_SQL_TAG,
					CobolPreprocessor.END_EXEC_TAG);
			result.setExecSqlText(execSqlText);

			registerASGElement(result);

			dataDescriptionEntries.add(result);
		}

		return result;
	}

	@Override
	public DataDescriptionEntryGroup addDataDescriptionEntryGroup(final DataDescriptionEntryFormat1Context ctx) {
		DataDescriptionEntryGroup result = (DataDescriptionEntryGroup) getASGElement(ctx);

		if (result == null) {
			final String name = determineName(ctx);
			result = new DataDescriptionEntryGroupImpl(name, this, programUnit, ctx);

			/*
			 * level number
			 */
			final Integer levelNumber;

			if (ctx.LEVEL_NUMBER_77() != null) {
				levelNumber = DataDescriptionEntry.LEVEL_NUMBER_SCALAR;
			} else if (ctx.INTEGERLITERAL() != null) {
				levelNumber = StringUtils.parseInteger(ctx.INTEGERLITERAL().getText());
			} else {
				levelNumber = null;
			}

			result.setLevelNumber(levelNumber);

			/*
			 * filler
			 */
			if (ctx.FILLER() != null) {
				result.setFiller(true);
			}

			/*
			 * aligned clause
			 */
			final DataAlignedClauseContext dataAlignedClauseContext = ctx.dataAlignedClause();

			if (dataAlignedClauseContext != null) {
				result.addAlignedClause(dataAlignedClauseContext);
			}

			/*
			 * blank when zero clause
			 */
			final List<DataBlankWhenZeroClauseContext> dataBlankWhenZeroClauseContexts = ctx.dataBlankWhenZeroClause();

			if (!dataBlankWhenZeroClauseContexts.isEmpty()) {
				final DataBlankWhenZeroClauseContext dataBlankWhenZeroClauseContext = dataBlankWhenZeroClauseContexts
						.get(0);
				result.addBlankWhenZeroClause(dataBlankWhenZeroClauseContext);
			}

			/*
			 * common own local clause
			 */
			final List<DataCommonOwnLocalClauseContext> dataCommonOwnLocalClauseContexts = ctx
					.dataCommonOwnLocalClause();

			if (!dataCommonOwnLocalClauseContexts.isEmpty()) {
				final DataCommonOwnLocalClauseContext dataCommonOwnLocalClauseContext = dataCommonOwnLocalClauseContexts
						.get(0);
				result.addCommonOwnLocalClause(dataCommonOwnLocalClauseContext);
			}

			/*
			 * external clause
			 */
			final DataExternalClauseContext dataExternalClauseContext = ctx.dataExternalClause();

			if (dataExternalClauseContext != null) {
				result.addExternalClause(dataExternalClauseContext);
			}

			/*
			 * global clause
			 */
			final DataGlobalClauseContext dataGlobalClauseContext = ctx.dataGlobalClause();

			if (dataGlobalClauseContext != null) {
				result.addGlobalClause(dataGlobalClauseContext);
			}

			/*
			 * data integer string clause
			 */
			final DataIntegerStringClauseContext dataIntegerStringClauseContext = ctx.dataIntegerStringClause();

			if (dataIntegerStringClauseContext != null) {
				result.addIntegerStringClause(dataIntegerStringClauseContext);
			}

			/*
			 * justified clause
			 */
			final List<DataJustifiedClauseContext> dataJustifiedClauseContexts = ctx.dataJustifiedClause();

			if (!dataJustifiedClauseContexts.isEmpty()) {
				final DataJustifiedClauseContext dataJustifiedClauseContext = dataJustifiedClauseContexts.get(0);
				result.addJustifiedClause(dataJustifiedClauseContext);
			}

			/*
			 * occurs clause
			 */
			final List<DataOccursClauseContext> dataOccursClauseContexts = ctx.dataOccursClause();

			for (final DataOccursClauseContext dataOccursClauseContext : dataOccursClauseContexts) {
				result.addOccursClause(dataOccursClauseContext);
			}

			/*
			 * picture clause
			 */
			final List<DataPictureClauseContext> dataPictureClauseContexts = ctx.dataPictureClause();

			if (!dataPictureClauseContexts.isEmpty()) {
				final DataPictureClauseContext dataPictureClauseContext = ctx.dataPictureClause().get(0);
				result.addPictureClause(dataPictureClauseContext);
			}

			/*
			 * received by clause
			 */
			final List<DataReceivedByClauseContext> dataReceivedByClauseContexts = ctx.dataReceivedByClause();

			if (!dataReceivedByClauseContexts.isEmpty()) {
				final DataReceivedByClauseContext dataReceivedByClauseContext = dataReceivedByClauseContexts.get(0);
				result.addReceivedByClause(dataReceivedByClauseContext);
			}

			/*
			 * record area clause
			 */
			final DataRecordAreaClauseContext dataRecordAreaClauseContext = ctx.dataRecordAreaClause();

			if (dataRecordAreaClauseContext != null) {
				result.addRecordAreaClause(dataRecordAreaClauseContext);
			}

			/*
			 * redefines clause
			 */
			final DataRedefinesClauseContext dataRedefinesClauseContext = ctx.dataRedefinesClause();

			if (dataRedefinesClauseContext != null) {
				result.addRedefinesClause(dataRedefinesClauseContext);
			}

			/*
			 * sign clause
			 */
			final List<DataSignClauseContext> dataSignClauseContexts = ctx.dataSignClause();

			if (!dataSignClauseContexts.isEmpty()) {
				final DataSignClauseContext dataSignClauseContext = dataSignClauseContexts.get(0);
				result.addSignClause(dataSignClauseContext);
			}

			/*
			 * synchronized
			 */
			final List<DataSynchronizedClauseContext> dataSynchronizedClauseContexts = ctx.dataSynchronizedClause();

			if (!dataSynchronizedClauseContexts.isEmpty()) {
				final DataSynchronizedClauseContext dataSynchronizedClauseContext = dataSynchronizedClauseContexts
						.get(0);
				result.addSynchronizedClause(dataSynchronizedClauseContext);
			}

			/*
			 * thread local
			 */
			final DataThreadLocalClauseContext dataThreadLocalClauseContext = ctx.dataThreadLocalClause();

			if (dataThreadLocalClauseContext != null) {
				result.addThreadLocalClause(dataThreadLocalClauseContext);
			}

			/*
			 * time type
			 */
			final List<DataTypeClauseContext> dataTypeClauseContexts = ctx.dataTypeClause();

			if (!dataTypeClauseContexts.isEmpty()) {
				final DataTypeClauseContext dataTypeClauseContext = dataTypeClauseContexts.get(0);
				result.addTypeClause(dataTypeClauseContext);
			}

			/*
			 * type def
			 */
			final DataTypeDefClauseContext dataTypeDefClauseContext = ctx.dataTypeDefClause();

			if (dataTypeDefClauseContext != null) {
				result.addTypeDefClause(dataTypeDefClauseContext);
			}

			/*
			 * usage
			 */
			final List<DataUsageClauseContext> dataUsageClauseContexts = ctx.dataUsageClause();

			if (!dataUsageClauseContexts.isEmpty()) {
				final DataUsageClauseContext dataUsageClauseContext = dataUsageClauseContexts.get(0);
				result.addUsageClause(dataUsageClauseContext);
			}

			/*
			 * using
			 */
			final List<DataUsingClauseContext> dataUsingClauseContexts = ctx.dataUsingClause();

			if (!dataUsingClauseContexts.isEmpty()) {
				final DataUsingClauseContext dataUsingClauseContext = dataUsingClauseContexts.get(0);
				result.addUsingClause(dataUsingClauseContext);
			}

			/*
			 * value
			 */
			final List<DataValueClauseContext> dataValueClauseContexts = ctx.dataValueClause();

			if (!dataValueClauseContexts.isEmpty()) {
				final DataValueClauseContext dataValueClauseContext = dataValueClauseContexts.get(0);
				result.addValueClause(dataValueClauseContext);
			}

			/*
			 * with lower bounds
			 */
			final DataWithLowerBoundsClauseContext dataWithLowerBoundsClauseContext = ctx.dataWithLowerBoundsClause();

			if (dataWithLowerBoundsClauseContext != null) {
				result.addWithLowerBoundClause(dataWithLowerBoundsClauseContext);
			}

			registerASGElement(result);

			dataDescriptionEntries.add(result);
			dataDescriptionEntriesSymbolTable.put(name, result);
		}

		return result;
	}

	@Override
	public DataDescriptionEntryRename addDataDescriptionEntryRename(final DataDescriptionEntryFormat2Context ctx) {
		DataDescriptionEntryRename result = (DataDescriptionEntryRename) getASGElement(ctx);

		if (result == null) {
			final String name = determineName(ctx);
			result = new DataDescriptionEntryRenameImpl(name, this, programUnit, ctx);

			result.setLevelNumber(DataDescriptionEntry.LEVEL_NUMBER_RENAME);
			result.addRenamesClause(ctx.dataRenamesClause());

			registerASGElement(result);

			dataDescriptionEntries.add(result);
			dataDescriptionEntriesSymbolTable.put(name, result);
		}

		return result;
	}

	@Override
	public DataDescriptionEntry createDataDescriptionEntry(
			final DataDescriptionEntryGroup currentDataDescriptionEntryGroup, final DataDescriptionEntryContext ctx) {
		final DataDescriptionEntry result;

		if (ctx.dataDescriptionEntryFormat1() != null) {
			result = addDataDescriptionEntryGroup(ctx.dataDescriptionEntryFormat1());
		} else if (ctx.dataDescriptionEntryFormat2() != null) {
			result = addDataDescriptionEntryRename(ctx.dataDescriptionEntryFormat2());
		} else if (ctx.dataDescriptionEntryFormat3() != null) {
			result = addDataDescriptionEntryCondition(ctx.dataDescriptionEntryFormat3());
		} else if (ctx.dataDescriptionEntryExecSql() != null) {
			result = addDataDescriptionEntryExecSql(ctx.dataDescriptionEntryExecSql());
		} else {
			LOG.warn("unknown data description entry {}", ctx);
			result = null;
		}

		if (currentDataDescriptionEntryGroup != null && result != null) {
			groupDataDescriptionEntry(currentDataDescriptionEntryGroup, result);
		}

		return result;
	}

	@Override
	public List<DataDescriptionEntry> getDataDescriptionEntries() {
		return dataDescriptionEntries;
	}

	@Override
	public DataDescriptionEntry getDataDescriptionEntry(final String name) {
		return dataDescriptionEntriesSymbolTable.get(name);
	}

	@Override
	public List<DataDescriptionEntry> getRootDataDescriptionEntries() {
		final List<DataDescriptionEntry> result = new ArrayList<DataDescriptionEntry>();

		for (final DataDescriptionEntry dataDescriptionEntry : dataDescriptionEntries) {

			if (dataDescriptionEntry.getParentDataDescriptionEntryGroup() == null) {
				result.add(dataDescriptionEntry);
			}
		}

		return result;
	}

	protected void groupDataDescriptionEntry(final DataDescriptionEntryGroup currentDataDescriptionEntryGroup,
			final DataDescriptionEntry dataDescriptionEntry) {
		final Integer currentLevelNumber = currentDataDescriptionEntryGroup.getLevelNumber();
		final Integer levelNumber = dataDescriptionEntry.getLevelNumber();

		if (!isGroupableLevelNumber(levelNumber)) {
		} else if (levelNumber > currentLevelNumber) {
			currentDataDescriptionEntryGroup.addDataDescriptionEntry(dataDescriptionEntry);
			dataDescriptionEntry.setParentDataDescriptionEntryGroup(currentDataDescriptionEntryGroup);
		} else {
			final DataDescriptionEntryGroup currentParentDataDescriptionEntryGroup = currentDataDescriptionEntryGroup
					.getParentDataDescriptionEntryGroup();

			if (currentParentDataDescriptionEntryGroup != null) {
				groupDataDescriptionEntry(currentParentDataDescriptionEntryGroup, dataDescriptionEntry);
			}
		}
	}

	protected boolean isGroupableLevelNumber(final Integer levelNumber) {
		final boolean result = levelNumber != null && DataDescriptionEntry.LEVEL_NUMBER_SCALAR != levelNumber
				&& DataDescriptionEntry.LEVEL_NUMBER_RENAME != levelNumber;
		return result;
	}
}
