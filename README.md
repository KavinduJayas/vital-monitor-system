# vital-monitor-system
A gateway interface for handling vital monitors in a hospital

## Background 

This project is based on a vital monitoring system in a hospital, where there is a
number of patients whose vitals (things like heart rate, blood pressure, etc.) should be
monitored. Each patient could be connected to a vital monitor and these monitors can
transmit the vital information over a network to a central location. That way nursing staff
are able to monitor many patients and do it remotely – which is useful when the
patients are contagious.

## Design

There is a set of vital monitors running. Each of these vital
monitors has an IP address. These vital monitors are running on a server. Vital
monitors broadcast their identity to a specific UDP port in the following format:

```<ip_addr, port, monitor_id>.```

![image](https://user-images.githubusercontent.com/59658804/157480845-edfe4ccd-cf71-4070-9a04-2ee0112caf11.png)

The ateway discovers all of these vital monitors and initiates TCP connections with
them. It uses the discovered port number and IP address for this
connection. This TCP connection will be used to receive vital information and
alarms from vital monitors.
