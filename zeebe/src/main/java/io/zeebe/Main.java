package io.zeebe;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.response.WorkflowInstanceEvent;
import io.zeebe.client.api.worker.JobWorker;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ashish Patel
 */
public class Main {


    public static void main(String[] args)
    {


        final ZeebeClient client = ZeebeClient.newClientBuilder()
            // change the contact point if needed
            .brokerContactPoint("127.0.0.1:26500")
            .build();

        System.out.println("Connected.");

        final DeploymentEvent deployment = client.newDeployCommand()
            .addResourceFromClasspath("order-process.bpmn")
            .send()
            .join();

        final int version = deployment.getWorkflows().get(0).getVersion();
        System.out.println("Workflow deployed. Version: " + version);




        final Map<String, Object> data = new HashMap<>();
        data.put("orderId", 31243);
        data.put("orderItems", Arrays.asList(435, 182, 376));

        final WorkflowInstanceEvent wfInstance = client.newCreateInstanceCommand()
            .bpmnProcessId("order-process")
            .latestVersion()
            .variables(data)
            .send()
            .join();

        final long workflowInstanceKey = wfInstance.getWorkflowInstanceKey();

        System.out.println("Workflow instance created. Key: " + workflowInstanceKey);


        final JobWorker collectMoney = client.newWorker().jobType("collect-money")

            .handler((jobClient, job) ->
            {


                final Map<String, Object> variables = job.getVariablesAsMap();

                System.out.println("Process orderId: " + variables.get("orderId"));
                double price = 46.50;
                System.out.println("Collect money: $" + price);

                // ...

              //  final Map<String, Object> result = new HashMap<>();
                variables.put("totalPrice", price);
                variables.put("Status_Collect_Money","Collect Money task has been completed");

                jobClient.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();
            })
           // .fetchVariables("orderId")
            .open();


        final JobWorker fetchItems = client.newWorker().jobType("fetch-items")

            .handler((jobClient, job) ->
            {


                final Map<String, Object> variables = job.getVariablesAsMap();

                System.out.println("Process orderId: " + variables.get("orderId"));
                System.out.println("Process orderItems: " + variables.get("orderItems"));


                // ...

                //  final Map<String, Object> result = new HashMap<>();

                variables.put("Status_Fetch_Items","FetchItems task has been completed");

                jobClient.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();
            })
            //.fetchVariables("orderItems")
            .open();



        final JobWorker shipParcel = client.newWorker().jobType("ship-parcel")

            .handler((jobClient, job) ->
            {


                final Map<String, Object> variables = job.getVariablesAsMap();

                System.out.println("Process orderId: " + variables.get("orderId"));
                System.out.println("Process orderItems: " + variables.get("orderItems"));


                // ...

                //  final Map<String, Object> result = new HashMap<>();

                variables.put("Status_Ship_Parcel","shipParcel task has been completed");

                jobClient.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();
            })
           // .fetchVariables("orderItems")
            .open();



//        jobWorker.close();
//        client.close();
//        System.out.println("Closed.");


    }

}
