## 1.0.0 (UNRELEASED)

BREAKING CHANGES:
* jdk1.7 end of support
* default Consul version 0.8.+

NEW FEATURES
* Added possibility to pass `raft_protocol` via `ConsulBuilder` 
which allows to test new [Consul Autopilot](https://www.consul.io/docs/guides/autopilot.html) 
feature 