package teetime.variant.methodcall.framework.core;

import teetime.util.list.CommittableQueue;

public interface Stage<I, O> {

	Object executeRecursively(Object element);

	O execute(Object element);

	// CommittableQueue<O> execute2();

	CommittableQueue<O> execute2(CommittableQueue<I> elements);

	// SchedulingInformation getSchedulingInformation();

	Stage<?, ?> getParentStage();

	void setParentStage(Stage<?, ?> parentStage, int index);

	// void setListener(OnDisableListener listener);

	Stage<?, ?> next();

	void setSuccessor(Stage<? super O, ?> successor);

	/**
	 * Used for execute4() (experiment02)
	 * 
	 * @return
	 */
	boolean isReschedulable();

	void onIsPipelineHead();

	void onStart();

}
