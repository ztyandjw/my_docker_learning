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


 ip neighbor add 172.18.20.0 lladdr ee:bb:57:1d:b1:9e dev flannel.1  
 bridge fdb append ee:bb:57:1d:b1:9e dev flannel.1 dst 10.0.94.216  
 ip route change 172.18.20.0/24 via 172.18.20.0 dev flannel.1  
 
ip neighbor add 172.18.10.0 lladdr 3e:26:90:b2:94:b1 dev flannel.1  
 bridge fdb append 3e:26:90:b2:94:b1 dev flannel.1 dst 10.0.94.147
ip route change 172.18.10.0/24 via 172.18.10.0 dev flannel.1  

* 测试  

ip netns exec docker1 ping -c 3 172.18.20.2  

* 删除  

ip link set br0 down  
brctl delbr br0  
ip link  del veth1  
ip link del flannel.1  
ip netns delete docker0  
ip netns delete docker1  
  












