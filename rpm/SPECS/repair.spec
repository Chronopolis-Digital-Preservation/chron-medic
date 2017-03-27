%define __jar_repack {%nil}

# For use below
%define _prefix %{_usr}/lib/chronopolis
%define _confdir /etc/chronopolis
%define service chron-repair
%define build_time %(date +"%Y%m%d")

Name: chron-repair
Version: %{ver}
Release: %{build_time}%{?dist}
Source: chron-repair.jar
Source1: chron-repair.sh
Source2: repair.yml
Summary: Chronopolis Repair Service
License: UMD
URL: https://gitlab.umiacs.umd.edu/chronopolis/medic
Group: System Environment/Daemons
Requires: /usr/sbin/groupadd /usr/sbin/useradd
autoprov: yes
autoreq: yes
BuildArch: noarch
BuildRoot: ${_tmppath}/build-%{name}-%{version}

%description
The Chronopolis Repair Service stages and replicates
content to be repaired in a preservation repository.

%install

rm -rf "%{buildroot}"
%__install -D -m0644 "%{SOURCE0}" "%{buildroot}%{_prefix}/%{service}.jar"

%__install -d "%{buildroot}/var/log/chronopolis"
%__install -d "%{buildroot}/etc/chronopolis"

%__install -D -m0755 "%{SOURCE1}" "%{buildroot}/etc/init.d/%{service}"
%__install -D -m0600 "%{SOURCE2}" "%{buildroot}%{_confdir}/repair.yml"


%files

%defattr(-,chronopolis,chronopolis)
# conf
%dir %{_confdir}
%config %attr(0644,-,-) %{_confdir}/repair.yml
# jar
%dir %attr(0755,chronopolis,chronopolis) %{_prefix}
%{_prefix}/%{service}.jar
# init/log
%config(noreplace) /etc/init.d/%{service}
%dir %attr(0755,chronopolis,chronopolis) /var/log/chronopolis

%pre
/usr/sbin/groupadd -r chronopolis > /dev/null 2>&1 || :
/usr/sbin/useradd -r -g chronopolis -c "Chronopolis Service User" \
        -s /bin/bash -d /usr/lib/chronopolis/ chronopolis > /dev/null 2>&1 || :
