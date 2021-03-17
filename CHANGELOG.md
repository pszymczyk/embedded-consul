# embedded-consul changelog

## 2.2.1 - Unreleased

 - bump default Consul version to 1.8.6
 - add Flag to Disable Addition of Shutdown Hook - [#113](https://github.com/pszymczyk/embedded-consul/pull/113/) - PR by [Eric Glass](https://github.com/ericnglass)
 - restore `groovy-xml` dependency - [#114](https://github.com/pszymczyk/embedded-consul/pull/114) - PR by [Martin Grigorov](https://github.com/martin-g)

## 2.2.0 - 2020-09-23

 - mass dependency update (including reduced set of provided Groovy dependencies) - [#100](https://github.com/pszymczyk/embedded-consul/pull/100), [#102](https://github.com/pszymczyk/embedded-consul/pull/102)
 - support for ARM64 - [#98](https://github.com/pszymczyk/embedded-consul/pull/98) - PR by [odidev](https://github.com/odidev)
 - automatic Continuous Delivery release process with [CDeliveryBoy](https://github.com/szpak/CDeliveryBoy) - [#103](https://github.com/pszymczyk/embedded-consul/pull/103) 
 - basic compatibility tests with OpenJDK 8, 11 and 15 on CI server - [#101](https://github.com/pszymczyk/embedded-consul/pull/101)
 - brand new changelog + sync with GitHub releases - [#37](https://github.com/pszymczyk/embedded-consul/issues/37)

## 2.0.0 - 2018-12-04

### BREAKING CHANGES
 
 - jdk1.7 end of support
 - default Consul version 0.8.+

### NEW FEATURES

 -  Added possibility to pass `raft_protocol` via `ConsulBuilder` which allows to test new [Consul Autopilot](https://www.consul.io/docs/guides/autopilot.html) feature

## 1.1.1 - 2018-06-01

 - last version supporting Java 1.7
