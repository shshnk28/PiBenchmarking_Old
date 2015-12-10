package helperClasses;

public class ExecutionPlan {
	public static String returnExecutionPlan (String planType) {
		String executionPlan = null;
		if(planType.equals("fil")) {
			executionPlan = "" +
	                "define stream cseEventStream (height int); " +
	                "" +
	                "@info(name = 'query1') " +
	                "from cseEventStream[height < 300] " +
	                "select height " +
	                "insert into outputStream ;";
		} else if (planType.equals("agg")) {
			executionPlan = "" +
    				"define stream cseEventStream (height int); " +
                    "" +
                    "@info(name = 'query1') " +
                    "from cseEventStream #window.length(5) " + 
                    "select avg(height) as avgHt " + 
                    "insert into outputStream ;";
			// tke the data from this stream and get an average
		} else if (planType.equals("seq")) {
			
		}
		return executionPlan;
	}
}
