package teetime.stage.taskfarm.monitoring.extraction;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import teetime.stage.taskfarm.monitoring.PipeMonitoringService;
import teetime.stage.taskfarm.monitoring.TaskFarmMonitoringData;
import teetime.stage.taskfarm.monitoring.SingleTaskFarmMonitoringService;

public class TimeBoundaryMPullThroughput3D extends AbstractSingleTaskFarmMonitoring {

	public TimeBoundaryMPullThroughput3D(final PipeMonitoringService pipeMonitoringService, final SingleTaskFarmMonitoringService taskFarmMonitoringService) {
		super(pipeMonitoringService, taskFarmMonitoringService);
	}

	@Override
	protected void writeCSVData(final Writer writer, final List<TaskFarmMonitoringData> monitoredDataValues) {
		try {
			addCSVLineToWriter(writer, "time", "boundary", "mpullthroughput");

			for (TaskFarmMonitoringData taskFarmMonitoringData : monitoredDataValues) {
				addCSVLineToWriter(writer,
						Long.toString(taskFarmMonitoringData.getTime()),
						Double.toString(taskFarmMonitoringData.getThroughputBoundary()),
						Double.toString(taskFarmMonitoringData.getMeanPullThroughput()));
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("The writer could not be written to: " + e.getMessage());
		}
	}
}
