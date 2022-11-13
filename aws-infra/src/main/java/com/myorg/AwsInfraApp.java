package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class AwsInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        AwsVpcStack vpcStack = new AwsVpcStack(app, "Vpc");
        AwsClusterStack clusterStack = new AwsClusterStack(app, "ClusterAlura", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        AwsRdsStack rdsStack = new AwsRdsStack(app, "Rds", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);

        AwsServiceStack service = new AwsServiceStack(app, "Service", clusterStack.getCluster());
        service.addDependency(clusterStack);
        service.addDependency(rdsStack);
        app.synth();
    }
}

