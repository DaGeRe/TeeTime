/**
 * Copyright (C) 2015 Christian Wulf, Nelson Tavares de Sousa (http://teetime.sourceforge.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package teetime.stage;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static teetime.framework.test.StageTester.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Nils Christian Ehmke
 */
public class InstanceCounterTest {

	private InstanceCounter<Object, Clazz> filter;

	@Before
	public void initializeFilter() {
		this.filter = new InstanceCounter<Object, Clazz>(Clazz.class);
	}

	@Test
	public void filterShouldCountCorrectTypes() {
		final Object clazz = new Clazz();

		test(this.filter).and().send(clazz).to(this.filter.getInputPort()).start();

		assertThat(this.filter.getCounter(), is(1));
	}

	@Test
	public void filterShouldCountSubTypes() {
		final Object clazz = new SubClazz();

		test(this.filter).and().send(clazz).to(this.filter.getInputPort()).start();

		assertThat(this.filter.getCounter(), is(1));
	}

	@Test
	public void filterShouldDropInvalidTypes() {
		final Object object = new Object();

		test(this.filter).and().send(object).to(this.filter.getInputPort()).start();

		assertThat(this.filter.getCounter(), is(0));
	}

	@Test
	public void filterShouldWorkWithMultipleInput() {
		final List<Object> input = new ArrayList<Object>();

		input.add(new Object());
		input.add(new Clazz());
		input.add(new Object());
		input.add(new SubClazz());
		input.add(new Object());

		test(this.filter).and().send(input).to(this.filter.getInputPort()).start();

		assertThat(this.filter.getCounter(), is(2));
	}

	private static class Clazz {
	}

	private static class SubClazz extends Clazz {
	}

}