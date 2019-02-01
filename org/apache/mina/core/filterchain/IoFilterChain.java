/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.filterchain;

import java.util.List;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public interface IoFilterChain {
	IoSession getSession();

	IoFilterChain.Entry getEntry(String arg0);

	IoFilterChain.Entry getEntry(IoFilter arg0);

	IoFilterChain.Entry getEntry(Class<? extends IoFilter> arg0);

	IoFilter get(String arg0);

	IoFilter get(Class<? extends IoFilter> arg0);

	NextFilter getNextFilter(String arg0);

	NextFilter getNextFilter(IoFilter arg0);

	NextFilter getNextFilter(Class<? extends IoFilter> arg0);

	List<IoFilterChain.Entry> getAll();

	List<IoFilterChain.Entry> getAllReversed();

	boolean contains(String arg0);

	boolean contains(IoFilter arg0);

	boolean contains(Class<? extends IoFilter> arg0);

	void addFirst(String arg0, IoFilter arg1);

	void addLast(String arg0, IoFilter arg1);

	void addBefore(String arg0, String arg1, IoFilter arg2);

	void addAfter(String arg0, String arg1, IoFilter arg2);

	IoFilter replace(String arg0, IoFilter arg1);

	void replace(IoFilter arg0, IoFilter arg1);

	IoFilter replace(Class<? extends IoFilter> arg0, IoFilter arg1);

	IoFilter remove(String arg0);

	void remove(IoFilter arg0);

	IoFilter remove(Class<? extends IoFilter> arg0);

	void clear() throws Exception;

	void fireSessionCreated();

	void fireSessionOpened();

	void fireSessionClosed();

	void fireSessionIdle(IdleStatus arg0);

	void fireMessageReceived(Object arg0);

	void fireMessageSent(WriteRequest arg0);

	void fireExceptionCaught(Throwable arg0);

	void fireInputClosed();

	void fireFilterWrite(WriteRequest arg0);

	void fireFilterClose();

	public interface Entry {
		String getName();

		IoFilter getFilter();

		NextFilter getNextFilter();

		void addBefore(String arg0, IoFilter arg1);

		void addAfter(String arg0, IoFilter arg1);

		void replace(IoFilter arg0);

		void remove();
	}
}