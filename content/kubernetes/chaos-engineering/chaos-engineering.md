---
weight: 71
title: Chaos Engineering


---

In the world of distributed computing we face a lot of new problems. Requirements change from steady stability into distributed architectures consisting of a multitude of services. Coming from the familiar world we take several things as granted, often called as the eight fallacies of distributed computing:

* The network is reliable
* There is zero latency
* Bandwidth is infinite
* The network is secure
* Topology never changes
* The network is homogeneous
* Consistent resource usage with no spikes
* All shared resources are available from all places

With supercharging our microservice architectures by following devops and gitops principles, we can rollout as fast as possible. Using the ability to make production ready deployments every few hours / days, we need to have confidence in our system. We can improve confidence by using testing principles. Software sided tests verify the integrity of our code. Infrastucutre test verify the theoretical and practical representation of our infrastructure as code. If we even go further, we use these combined and test our system end to end automatically.

There is also the other side of the medal. Even with having all the tests described above, we still should be on our toes. Classical approaches of testing verify the desired and intended state / workflow of our systems. What about the unpredictable? What about chaos?

## Principles of chaos engineering

Chaos Engineering describes the discipline of creating scanarios simulating turbulent conditions to build confidence in our systems robustness.

Even when all of the individual services in a distributed system are functioning properly, the interactions between those services can cause unpredictable outcomes. Unpredictable outcomes, compounded by rare but disruptive real-world events that affect production environments, make these distributed systems inherently chaotic.

An empirical, systems-based approach addresses the chaos in distributed systems at scale and builds confidence in the ability of those systems to withstand realistic conditions.

## Chaos in practice

To specifically address the uncertainty of distributed systems at scale, Chaos Engineering can be thought of as the facilitation of experiments to uncover systemic weaknesses. These experiments follow four steps:

1. Start by defining ‘steady state’ as some measurable output of a system that indicates normal behavior.
2. Hypothesize that this steady state will continue in both the control group and the experimental group.
3. Introduce variables that reflect real world events like servers that crash, hard drives that malfunction, network connections that are severed, etc.
4. Try to disprove the hypothesis by looking for a difference in steady state between the control group and the experimental group.

The harder it is to disrupt the steady state, the more confidence we have in the behavior of the system. If a weakness is uncovered, we now have a target for improvement before that behavior manifests in the system at large.

## ADVANCED PRINCIPLES

The following principles describe an ideal application of Chaos Engineering, applied to the processes of experimentation described above. The degree to which these principles are pursued strongly correlates to the confidence we can have in a distributed system at scale.
Build a Hypothesis around Steady State Behavior

Focus on the measurable output of a system, rather than internal attributes of the system. Measurements of that output over a short period of time constitute a proxy for the system’s steady state. The overall system’s throughput, error rates, latency percentiles, etc. could all be metrics of interest representing steady state behavior. By focusing on systemic behavior patterns during experiments, Chaos verifies that the system does work, rather than trying to validate how it works.

### Vary Real-world Events

Chaos variables reflect real-world events. Prioritize events either by potential impact or estimated frequency. Consider events that correspond to hardware failures like servers dying, software failures like malformed responses, and non-failure events like a spike in traffic or a scaling event. Any event capable of disrupting steady state is a potential variable in a Chaos experiment.

### Run Experiments in Production

Systems behave differently depending on environment and traffic patterns. Since the behavior of utilization can change at any time, sampling real traffic is the only way to reliably capture the request path. To guarantee both authenticity of the way in which the system is exercised and relevance to the current deployed system, Chaos strongly prefers to experiment directly on production traffic.

### Automate Experiments to Run Continuously

Running experiments manually is labor-intensive and ultimately unsustainable. Automate experiments and run them continuously. Chaos Engineering builds automation into the system to drive both orchestration and analysis.

### Minimize Blast Radius

Experimenting in production has the potential to cause unnecessary customer pain. While there must be an allowance for some short-term negative impact, it is the responsibility and obligation of the Chaos Engineer to ensure the fallout from experiments are minimized and contained.

Chaos Engineering is a powerful practice that is already changing how software is designed and engineered at some of the largest-scale operations in the world. Where other practices address velocity and flexibility, Chaos specifically tackles systemic uncertainty in these distributed systems. The Principles of Chaos provide confidence to innovate quickly at massive scales and give customers the high quality experiences they deserve.

## Test Environment Recommendations - how and where to run chaos tests

* Run the chaos tests continuously in your test pipelines:
  * Software, systems, and infrastructure does change – and the condition/health of each can change pretty rapidly. A good place to run tests is in your CI/CD pipeline running on a regular cadence.
* Run the chaos tests manually to learn from the system:
  * hen running a Chaos scenario or Fault tests, it is more important to understand how the system responds and reacts, rather than mark the execution as pass or fail.
  * It is important to define the scope of the test before the execution to avoid some issues from masking others.
* Run the chaos tests in production environments or mimic the load in staging environments:
  * As scary as a thought about testing in production is, production is the environment that users are in and traffic spikes/load are real. To fully test the robustness/resilience of a production system, running Chaos Engineering experiments in a production environment will provide needed insights. A couple of things to keep in mind:
    * Minimize blast radius and have a backup plan in place to make sure the users and customers do not undergo downtime.
    * Mimic the load in a staging environment in case Service Level Agreements are too tight to cover any downtime.
* Enable Observability:
  * Chaos Engineering Without Observability … Is Just Chaos.
  * Make sure to have logging and monitoring installed on the cluster to help with understanding the behaviour as to why it is happening. In case of running the tests in the CI where it is not humanly possible to monitor the cluster all the time, it is recommended to leverage Cerberus to capture the state during the runs and metrics collection in Kraken to store metrics long term even after the cluster is gone.
  * Kraken ships with dashboards that will help understand API, Etcd and OpenShift cluster level stats and performance metrics.
  * Pay attention to Prometheus alerts. Check if they are firing as expected.
* Run multiple chaos tests at once to mimic the production outages:
  * For example, hogging both IO and Network at the same time instead of running them separately to observe the impact.
  * You might have existing test cases, be it related to Performance, Scalability or QE. Run the chaos in the background during the test runs to observe the impact. Signaling feature in Kraken can help with coordinating the chaos runs i.e., start, stop, pause the scenarios based on the state of the other test jobs.
