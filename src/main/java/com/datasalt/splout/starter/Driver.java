package com.datasalt.splout.starter;

import org.apache.hadoop.util.ProgramDriver;

public class Driver extends ProgramDriver {

	public Driver() throws Throwable {
		addClass("generate", GenerateTablespace.class, "Creates a simple tablespace called splout-starter with two tables.");
		addClass("deploy", DeployTablespace.class, "Deploys the splout-starter tablespace to the local QNode at http://localhost:4412.");
	}
	
	public static void main(String args[]) throws Throwable {
		new Driver().driver(args);
	}
}
