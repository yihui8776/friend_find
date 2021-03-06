/**
 * 
 */
package com.fz.fastcluster;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;

import com.fz.fastcluster.mr.ClusterDataMapper;
import com.fz.filter.keytype.DoubleArrIntWritable;
import com.fz.util.HUtils;


/**
 * cluster data 
 * 
 * 输入为 db2hdfs的输出
 * 
 * @author fansy
 * @date 2015-6-2
 */
public class ClusterDataJob extends Configured implements Tool {

	public int run(String[] args) throws Exception {
		
		Configuration conf = HUtils.getConf();
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	    if (otherArgs.length !=5) {
	      System.err.println("Usage: fz.fast_cluster.ClusterData <in> <out>" +
	      		" <K> <dc> <iter_i>");
	      System.exit(5);
	    }
	    conf.setInt("K", Integer.parseInt(otherArgs[2]));
	    conf.setDouble("DC", Double.parseDouble(otherArgs[3]));
	    conf.setInt("ITER_I", Integer.parseInt(otherArgs[4]));
	    Job job =  Job.getInstance(conf,"cluster data with iteration: "+otherArgs[4]+",dc阈值："+otherArgs[3]);
	    job.setJarByClass(ClusterDataJob.class);
	    job.setMapperClass(ClusterDataMapper.class);
	    job.setNumReduceTasks(0);
	    
	    // <id,<type,用户有效向量>>
	    MultipleOutputs.addNamedOutput(job, "clustered", SequenceFileOutputFormat.class,  
                IntWritable.class, DoubleArrIntWritable.class);
	    // <id,<type,用户有效向量>>
        MultipleOutputs.addNamedOutput(job, "unclustered", SequenceFileOutputFormat.class,  
        		IntWritable.class, DoubleArrIntWritable.class);  
	    
	    job.setInputFormatClass(SequenceFileInputFormat.class);
	    job.setOutputFormatClass(SequenceFileOutputFormat.class);
	    
	    SequenceFileInputFormat.addInputPath(job, new Path(otherArgs[0]));
	    SequenceFileOutputFormat.setOutputPath(job,new Path(otherArgs[1]));
	    
	    FileSystem.get(conf).delete(new Path(otherArgs[1]), true);
	    int ret= job.waitForCompletion(true) ? 0 : 1;
	    
	    // 把已经分类的个数和未分类的个数赋值出去
	    HUtils.CLUSTERED=job.getCounters().findCounter(ClusterCounter.CLUSTERED).getValue();
	    HUtils.UNCLUSTERED=job.getCounters().findCounter(ClusterCounter.UNCLUSTERED).getValue();
	    return ret;
	}

	
}
