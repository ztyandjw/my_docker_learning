yum clean all
yum install -y epel-release  
yum install -y conntrack ipvsadm ipset jq sysstat curl iptables libseccomp lrzsz net-tools bridge-utils


## VXLAN

虚拟可扩展局域网，减少了用户态到内核态的切换次数，把核心处理都放在内核态进行。
在内核态进行封装和解封装的功能在现有的三层网络上，覆盖一层虚拟的由内核VXLAN负
责维护的二层网路。  
为了能够在二层网络打通隧道，VXLAN会在宿主机上设置一个特殊的网络设备作为隧道的两端
，这个设备叫做VTEP，VXLAN Tunnel End Point。


* 分别配置内核参数  

sysctl net.ipv4.ip_forward=1  

* 分别创建容器  

ip netns add docker1  
ip netns add docker2

* 分别创建veth pair  

ip link add veth0 type veth peer name veth1  
ip link add veth0 type veth peer name veth1  

* 将veth的一端放入容器  

ip link set veth0 netns docker1  
ip link set veth0 netns docker2  

* 创建bridge  

brctl addbr br0  

* veth宿主机那端接入网桥  

brctl addif br0 veth1  
brctl addif br0 veth1  

* 为容器内的网卡分配ip地址，并且激活  

ip netns exec docker1 ip addr add 172.18.10.2/24 dev veth0  
ip netns exec docker1 ip link set veth0 up  

ip netns exec docker2 ip addr add 172.18.20.2/24 dev veth0  
ip netns exec docker2 ip link set veth0 up 

* 为宿主机端veth激活
  
ip link set veth1 up   

* 为bridge分配ip地址，并且激活  

ip addr add 172.18.10.1/24 dev br0  
ip link set br0 up  

ip addr add 172.18.20.1/24 dev br0  
ip link set br0 up  

* 将bridge设置为缺省网关  

ip netns exec docker1 route add default gw 172.18.10.1 veth0  
ip netns exec docker2 route add default gw 172.18.20.1 veth0  

* 创建VTEP，分配ip并且激活

ip link add flannel.1 type vxlan id 1 local 10.0.94.147 dev eth0 dstport 4789 nolearning  
ip addr add 172.18.10.0/32 dev flannel.1  
ip link set flannel.1 up  
ip route add 172.18.20.0/24 dev flannel.1

ip link add flannel.1 type vxlan id 1 local 10.0.94.216 dev eth0 dstport 4789 nolearning  
ip addr add 172.18.20.0/32 dev flannel.1  
ip link set flannel.1 up   
ip route add 172.18.10.0/24 dev flannel.1 scope global


 ip neighbor add 172.18.20.2 lladdr 2a:6b:c1:17:46:84 dev flannel.1  
 bridge fdb append 2a:6b:c1:17:46:84 dev flannel.1 dst 10.0.94.216  
 
ip neighbor add 172.18.10.2 lladdr fe:82:1a:e3:f8:b6 dev flannel.1  
 bridge fdb append fe:82:1a:e3:f8:b6 dev flannel.1 dst 10.0.94.147

ip route change 172.18.20.0/24 via 172.18.20.0 dev flannel.1  












```

[root@10-0-94-147 yum.repos.d]# arp -n
Address                  HWtype  HWaddress           Flags Mask            Iface
10.0.94.245              ether   fa:a0:52:3a:cc:00   C                     eth0
10.0.94.148              ether   66:22:1f:3a:07:76   C                     eth0
10.0.94.1                ether   60:da:83:7c:25:6b   C                     eth0
172.18.20.0                      (incomplete)                              vxlan100


```

```

[root@10-0-94-147 yum.repos.d]# bridge fdb
01:00:5e:00:00:01 dev eth0 self permanent
33:33:00:00:00:01 dev eth0 self permanent
33:33:ff:28:f2:00 dev eth0 self permanent
33:33:00:00:00:01 dev veth1 self permanent
01:00:5e:00:00:01 dev veth1 self permanent
33:33:ff:5d:a5:4c dev veth1 self permanent
fe:8e:94:5d:a5:4c dev veth1 vlan 1 master br0 permanent
3e:fa:2d:81:a2:07 dev br0 vlan 1 master br0 permanent
fe:8e:94:5d:a5:4c dev veth1 master br0 permanent


```