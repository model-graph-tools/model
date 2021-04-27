# Model

Microservice exposing a REST API to query the WildFly management model. The model service is bound to one graph database containing one specific version of the management model.

## Big Picture

The model service is part of the [model graph tools](https://model-graph-tools.github.io/) and uses the graph database created by the [analyzer](https://github.com/model-graph-tools/analyzer) command line tool.

Take a look at the [setup](https://github.com/model-graph-tools/setup) repository how to get started.

<img src="https://model-graph-tools.github.io/img/tools.svg" alt="Model Graph Tools" width="512" />