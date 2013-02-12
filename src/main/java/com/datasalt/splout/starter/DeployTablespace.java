package com.datasalt.splout.starter;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.splout.db.hadoop.StoreDeployerTool;
import com.splout.db.hadoop.TablespaceDepSpec;

public class DeployTablespace implements Tool {

	// the hadoop configuration
	private Configuration hadoopConf;
	
	@Override
  public Configuration getConf() {
	  return hadoopConf;
  }

	@Override
  public void setConf(Configuration hadoopConf) {
		this.hadoopConf = hadoopConf;
  }

	@Override
  public int run(String[] ignoreArgs) throws Exception {
		String qNode = "http://localhost:4412";
		
		// 2) finally, deploy the generated files
		StoreDeployerTool deployer = new StoreDeployerTool(qNode, hadoopConf);
		List<TablespaceDepSpec> specs = new ArrayList<TablespaceDepSpec>();
		specs.add(new TablespaceDepSpec("splout-starter", GenerateTablespace.INDEXED_FILES_OUT_DIR, 1, null));

		deployer.deploy(specs);
		
		return 1;
  }
	
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new DeployTablespace(), args);
	}
}
