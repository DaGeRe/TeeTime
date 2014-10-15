package teetime.examples.experiment14;

import teetime.examples.experiment01.MethodCallThoughputTimestampAnalysis1Test;
import util.test.MeasurementRepository;
import util.test.PerformanceResult;
import util.test.PerformanceTest;
import util.test.ProfiledPerformanceAssertion;

public abstract class AbstractPerformanceCheck extends ProfiledPerformanceAssertion {

	protected PerformanceResult test01;
	protected PerformanceResult test14;

	@Override
	public void check() {
		String testMethodIdentifier = MeasurementRepository.buildTestMethodIdentifier(MethodCallThoughputTimestampAnalysis1Test.class, "testWithManyObjects");
		test01 = PerformanceTest.measurementRepository.performanceResults.get(testMethodIdentifier);
		testMethodIdentifier = MeasurementRepository.buildTestMethodIdentifier(MethodCallThoughputTimestampAnalysis14Test.class, "testWithManyObjects");
		test14 = PerformanceTest.measurementRepository.performanceResults.get(testMethodIdentifier);
	}

}