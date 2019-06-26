package org.thrift.agent.pojo;

import io.jaegertracing.thriftjava.Process;
import io.jaegertracing.thriftjava.Span;

public class AgentSpan {
	
	private Span span;
	
	private Process process;

	public AgentSpan(Span span, Process process) {
		super();
		this.span = span;
		this.process = process;
	}

	public Span getSpan() {
		return span;
	}

	public void setSpan(Span span) {
		this.span = span;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}
}
