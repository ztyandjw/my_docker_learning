#kubernetes单机版

##安装前准备活动

####机器环境

* 10.0.94.164(kebe-master1)
* 10.0.94.136(kebe-node1)
* 10.0.94.234(kebe-node2)
* 10.0.94.216
* 10.0.94.147
* 10.0.94.245

####主要软件版本

* Kubernetes 1.12.2
* Docker 18.03.1-ce
* Etcd 3.3.10

####系统初始化&全局变量

######主机名
* `hostnamectl set-hostname hostname`
* 修改/etc/hosts文件

######无密码登录其它节点
* `ssh-keygen -t rsa`
* `ssh-copy-id root@${hostname}`

######将可执行文件路径添加到PATH路径下
* echo 'PATH=/opt/k8s/bin:$PATH' >> /root/.bash

######安装软件包
* `yum install -y epel-release`
* `yum install -y conntrack ipvsadm ipset jq sysstat curl iptables libseccomp lrzsz`


######关闭防火墙
 `systemctl stop firewalld`
 
 `systemctl disable firewalld`
 
`iptables -F && sudo iptables -X && sudo iptables -F -t nat && sudo iptables -X -t nat`

 `iptables -P FORWARD ACCEPT`

######关闭swap分区
* `swapoff -a`
* `sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab`

######关闭selinux
`setenforce 0`

 `grep SELINUX /etc/selinux/config`

######关闭dnsmasq
`service dnsmasq stop`

`systemctl disable dnsmasq`

######设置系统参数
```
   cat > kubernetes.conf <<EOF
   net.bridge.bridge-nf-call-iptables=1
   net.bridge.bridge-nf-call-ip6tables=1
   net.ipv4.ip_forward=1
   vm.swappiness=0
   vm.overcommit_memory=1
   vm.panic_on_oom=0
   fs.inotify.max_user_watches=89100
   EOF
   ```
   
```cp kubernetes.conf  /etc/sysctl.d/kubernetes.conf```

```sysctl -p /etc/sysctl.d/kubernetes.conf```

```mount -t cgroup -o cpu,cpuacct none /sys/fs/cgroup/cpu,cpuacct```


######加载内核模块
`modprobe br_netfilter`

 `modprobe ip_vs`
 
 ######设置系统时区
 `timedatectl set-timezone Asia/Shanghai`
 
 `timedatectl set-local-rtc 0`
 
 `systemctl restart rsyslog`
 
 `systemctl restart crond`
 
  ######创建目录
  
  `mkdir -p /opt/k8s/bin`
  
  `mkdir -p /etc/kubernetes/cert`
  
  `mkdir -p /etc/etcd/cert`
  
  `mkdir -p /var/lib/etcd`
 
##创建CA证书以及秘钥
 
####安装cfssl工具集

`mkdir -p /opt/k8s/cert && cd /opt/k8s`

`wget https://pkg.cfssl.org/R1.2/cfssl_linux-amd64`

`mv cfssl_linux-amd64 /opt/k8s/bin/cfssl`

`wget https://pkg.cfssl.org/R1.2/cfssljson_linux-amd64`

`mv cfssljson_linux-amd64 /opt/k8s/bin/cfssljson`

`wget https://pkg.cfssl.org/R1.2/cfssl-certinfo_linux-amd64`

`mv cfssl-certinfo_linux-amd64 /opt/k8s/bin/cfssl-certinfo`

`chmod +x /opt/k8s/bin/*`

`export PATH=/opt/k8s/bin:$PATH`


####创建CA根证书
CA 证书是集群所有节点共享的，只需要创建一个 CA 证书，后续创建的所有证书都由它签名

######创建配置文件
```
cat > ca-config.json <<EOF
{
  "signing": {
    "default": {
      "expiry": "87600h"
    },
    "profiles": {
      "kubernetes": {
        "usages": [
            "signing",
            "key encipherment",
            "server auth",
            "client auth"
        ],
        "expiry": "87600h"
      }
    }
  }
}
EOF

```
CA 配置文件用于配置根证书的使用场景 (profile) 和具体参数 (usage，过期时间、服务端认证、客户端认证、加密等)，后续在签名其它证书时需要指定特定场景。


######创建证书请求文件
```
cat > ca-csr.json <<EOF
{
  "CN": "kubernetes",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "BeiJing",
      "L": "BeiJing",
      "O": "k8s",
      "OU": "4Paradigm"
    }
  ]
}
EOF
```

######生成 CA 证书和私钥

`cfssl gencert -initca ca-csr.json | cfssljson -bare ca`

##部署 kubectl 命令行工具

####下载和分发kubectl二进制文件


####创建admin证书和私钥

```
cat > admin-csr.json <<EOF
{
  "CN": "admin",
  "hosts": [],
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "BeiJing",
      "L": "BeiJing",
      "O": "system:masters",
      "OU": "4Paradigm"
    }
  ]
}
EOF
```

```
cfssl gencert -ca=/etc/kubernetes/cert/ca.pem \
  -ca-key=/etc/kubernetes/cert/ca-key.pem \
  -config=/etc/kubernetes/cert/ca-config.json \
  -profile=kubernetes admin-csr.json | cfssljson -bare admin
ls admin*
```

####创建 kubeconfig 文件

kubeconfig 为 kubectl 的配置文件，包含访问 apiserver 的所有信息，如 apiserver 地址、CA 证书和自身使用的证书

######设置集群参数
```
kubectl config set-cluster kubernetes \
  --certificate-authority=/etc/kubernetes/cert/ca.pem \
  --embed-certs=true \
  --server=https://10.0.94.164:6443 \
  --kubeconfig=kubectl.kubeconfig
  
```

######设置客户端认证参数
```
kubectl config set-credentials admin \
  --client-certificate=admin.pem \
  --client-key=admin-key.pem \
  --embed-certs=true \
  --kubeconfig=kubectl.kubeconfig
  
```

######设置上下文参数

```
kubectl config set-context kubernetes \
  --cluster=kubernetes \
  --user=admin \
  --kubeconfig=kubectl.kubeconfig

```

###### 设置默认上下文
```
kubectl config use-context kubernetes --kubeconfig=kubectl.kubeconfig
```

#### 分发kubeconfig文件

`mkdir -p ~/.kube`

`cp kubectl.kubeconfig ~/.kube/config`

##部署etcd

####创建etcd证书与私钥

创建证书签名请求
```
cat > etcd-csr.json <<EOF
{
  "CN": "etcd",
  "hosts": [
    "127.0.0.1",
    "10.0.94.164"
  ],
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "BeiJing",
      "L": "BeiJing",
      "O": "k8s",
      "OU": "4Paradigm"
    }
  ]
}
EOF
```

生成证书和私钥

```
cfssl gencert -ca=/etc/kubernetes/cert/ca.pem \
    -ca-key=/etc/kubernetes/cert/ca-key.pem \
    -config=/etc/kubernetes/cert/ca-config.json \
    -profile=kubernetes etcd-csr.json | cfssljson -bare etcd 
```

分发生成的证书和私钥到etcd证书目录
```
cp etcd*.pem /etc/etcd/cert
```

#### 创建 etcd 的 systemd unit 模板文件

```
[Unit]
Description=Etcd Server
After=network.target
After=network-online.target
Wants=network-online.target
Documentation=https://github.com/coreos

[Service]
User=root
Type=notify
WorkingDirectory=/var/lib/etcd/
ExecStart=/opt/k8s/bin/etcd \
  --data-dir=/var/lib/etcd \
  --name=kube-master1 \
  --cert-file=/etc/etcd/cert/etcd.pem \
  --key-file=/etc/etcd/cert/etcd-key.pem \
  --trusted-ca-file=/etc/kubernetes/cert/ca.pem \
  --peer-cert-file=/etc/etcd/cert/etcd.pem \
  --peer-key-file=/etc/etcd/cert/etcd-key.pem \
  --peer-trusted-ca-file=/etc/kubernetes/cert/ca.pem \
  --peer-client-cert-auth \
  --client-cert-auth \
  --listen-peer-urls=https://10.0.94.164:2380 \
  --initial-advertise-peer-urls=https://10.0.94.164:2380 \
  --listen-client-urls=https://10.0.94.164:2379,http://127.0.0.1:2379 \
  --advertise-client-urls=https://10.0.94.164:2379
Restart=on-failure
RestartSec=5
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```

`mkdir -p /var/lib/etcd`


#### 启动etcd服务

`systemctl daemon-reload && systemctl enable etcd && systemctl restart etcd`

#### 检查

`systemctl status etcd|grep Active`

`journalctl -u etcd`

#### 验证

```
ETCDCTL_API=3 /opt/k8s/bin/etcdctl \
    --endpoints=https://10.0.94.164:2379 \
    --cacert=/etc/kubernetes/cert/ca.pem \
    --cert=/etc/etcd/cert/etcd.pem \
    --key=/etc/etcd/cert/etcd-key.pem endpoint health
```

## 安装kube-apiserver

#### 创建证书与私钥
```
source /opt/k8s/bin/environment.sh
cat > kubernetes-csr.json <<EOF
{
  "CN": "kubernetes",
  "hosts": [
    "127.0.0.1",
    "10.0.94.164",
    "10.254.0.1",
    "kubernetes",
    "kubernetes.default",
    "kubernetes.default.svc",
    "kubernetes.default.svc.cluster",
    "kubernetes.default.svc.cluster.local"
  ],
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "ST": "BeiJing",
      "L": "BeiJing",
      "O": "k8s",
      "OU": "4Paradigm"
    }
  ]
}
EOF

```

```
cfssl gencert -ca=/etc/kubernetes/cert/ca.pem \
  -ca-key=/etc/kubernetes/cert/ca-key.pem \
  -config=/etc/kubernetes/cert/ca-config.json \
  -profile=kubernetes kubernetes-csr.json | cfssljson -bare kubernetes
ls kubernetes*pem

```

`cp kubernetes*.pem /etc/kubernetes/cert`

#### 创建加密配置文件

```

cat > encryption-config.yaml <<EOF
kind: EncryptionConfig
apiVersion: v1
resources:
  - resources:
      - secrets
    providers:
      - aescbc:
          keys:
            - name: key1
              secret: $(head -c 32 /dev/urandom | base64)
      - identity: {}
EOF

```

`cp encryption-config.yaml /etc/kubernetes`

#### 创建kube-apiserver systemd unit
```

cat > kube-apiserver.service <<EOF
[Unit]
Description=Kubernetes API Server
Documentation=https://github.com/GoogleCloudPlatform/kubernetes
After=network.target

[Service]
ExecStart=/opt/k8s/bin/kube-apiserver \\
  --enable-admission-plugins=Initializers,NamespaceLifecycle,NodeRestriction,LimitRanger,ServiceAccount,DefaultStorageClass,ResourceQuota \\
  --anonymous-auth=false \\
  --experimental-encryption-provider-config=/etc/kubernetes/encryption-config.yaml \\
  --advertise-address=10.0.94.164 \\
  --bind-address=10.0.94.164 \\
  --insecure-port=0 \\
  --authorization-mode=Node,RBAC \\
  --runtime-config=api/all \\
  --enable-bootstrap-token-auth \\
  --service-cluster-ip-range=10.254.0.0/16 \\
  --service-node-port-range=1-40000 \\
  --tls-cert-file=/etc/kubernetes/cert/kubernetes.pem \\
  --tls-private-key-file=/etc/kubernetes/cert/kubernetes-key.pem \\
  --client-ca-file=/etc/kubernetes/cert/ca.pem \\
  --kubelet-client-certificate=/etc/kubernetes/cert/kubernetes.pem \\
  --kubelet-client-key=/etc/kubernetes/cert/kubernetes-key.pem \\
  --service-account-key-file=/etc/kubernetes/cert/ca-key.pem \\
  --etcd-cafile=/etc/kubernetes/cert/ca.pem \\
  --etcd-certfile=/etc/kubernetes/cert/kubernetes.pem \\
  --etcd-keyfile=/etc/kubernetes/cert/kubernetes-key.pem \\
  --etcd-servers=https://10.0.94.164:2379 \\
  --enable-swagger-ui=true \\
  --allow-privileged=true \\
  --audit-log-maxage=30 \\
  --audit-log-maxbackup=3 \\
  --audit-log-maxsize=100 \\
  --audit-log-path=/var/log/kube-apiserver-audit.log \\
  --event-ttl=1h \\
  --alsologtostderr=true \\
  --logtostderr=false \\
  --log-dir=/var/log/kubernetes \\
  --v=2
Restart=on-failure
RestartSec=5
Type=notify
User=root
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF

```

`cp kube-apiserver.service /etc/systemd/system`

#### 启动 kube-apiserver 服务

`systemctl daemon-reload && systemctl enable kube-apiserver && systemctl restart kube-apiserver`


#### 将kube-apiserver信息写入etcd

```

ETCDCTL_API=3 etcdctl \
    --endpoints=https://10.0.94.164:2379 \
    --cacert=/etc/kubernetes/cert/ca.pem \
    --cert=/etc/etcd/cert/etcd.pem \
    --key=/etc/etcd/cert/etcd-key.pem \
    get /registry/ --prefix --keys-only
    
```
   
#### 检查集群信息

`kubectl cluster-info`

`kubectl get all --all-namespaces`

`kubectl get componentstatuses`

#### 授权apiserver调用kubelet的api权限

`kubectl create clusterrolebinding kube-apiserver:kubelet-apis --clusterrole=system:kubelet-api-admin --user kubernetes`


## kube-controller-manager安装

#### 创建证书秘钥

```

cat > kube-controller-manager-csr.json <<EOF
{
    "CN": "system:kube-controller-manager",
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "hosts": [
      "127.0.0.1",
      "10.0.94.164"
    ],
    "names": [
      {
        "C": "CN",
        "ST": "BeiJing",
        "L": "BeiJing",
        "O": "system:kube-controller-manager",
        "OU": "4Paradigm"
      }
    ]
}
EOF

```

`cp kube-controller-manager*.pem /etc/kubernetes/cert`

#### 创建和分发kube-controller-manager的配置文件

```

kubectl config set-cluster kubernetes \
--certificate-authority=/etc/kubernetes/cert/ca.pem \
--embed-certs=true \
--server=https://10.0.94.164:6443 \
--kubeconfig=kube-controller-manager.kubeconfig

```


```

kubectl config set-credentials system:kube-controller-manager \
--client-certificate=kube-controller-manager.pem \
--client-key=kube-controller-manager-key.pem \
--embed-certs=true \
--kubeconfig=kube-controller-manager.kubeconfig

```

```
kubectl config set-context system:kube-controller-manager \
--cluster=kubernetes \
--user=system:kube-controller-manager \
--kubeconfig=kube-controller-manager.kubeconfig

```

```
kubectl config use-context system:kube-controller-manager --kubeconfig=kube-controller-manager.kubeconfig
```

`cp kube-controller-manager.kubeconfig /etc/kubernetes`


#### 创建和分发system unit 文件

```

cat > kube-controller-manager.service <<EOF
[Unit]
Description=Kubernetes Controller Manager
Documentation=https://github.com/GoogleCloudPlatform/kubernetes

[Service]
ExecStart=/opt/k8s/bin/kube-controller-manager \\
  --kubeconfig=/etc/kubernetes/kube-controller-manager.kubeconfig \\
  --service-cluster-ip-range=10.254.0.0/16 \\
  --cluster-name=kubernetes \\
  --cluster-signing-cert-file=/etc/kubernetes/cert/ca.pem \\
  --cluster-signing-key-file=/etc/kubernetes/cert/ca-key.pem \\
  --experimental-cluster-signing-duration=8760h \\
  --root-ca-file=/etc/kubernetes/cert/ca.pem \\
  --service-account-private-key-file=/etc/kubernetes/cert/ca-key.pem \\
  --feature-gates=RotateKubeletServerCertificate=true \\
  --controllers=*,bootstrapsigner,tokencleaner \\
  --horizontal-pod-autoscaler-use-rest-clients=true \\
  --horizontal-pod-autoscaler-sync-period=10s \\
  --tls-cert-file=/etc/kubernetes/cert/kube-controller-manager.pem \\
  --tls-private-key-file=/etc/kubernetes/cert/kube-controller-manager-key.pem \\
  --use-service-account-credentials=true \\
  --alsologtostderr=true \\
  --logtostderr=false \\
  --log-dir=/var/log/kubernetes \\
  --v=2
Restart=on
Restart=on-failure
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
EOF

```

`cp kube-controller-manager.service /etc/systemd/system`

#### 启动controller-manager
`systemctl daemon-reload && systemctl enable kube-controller-manager && systemctl restart kube-controller-manager`


## 部署kube-scheduler集群

#### 证书签名请求与创建证书

```

cat > kube-scheduler-csr.json <<EOF
{
    "CN": "system:kube-scheduler",
    "hosts": [
      "127.0.0.1",
      "10.0.94.164"
    ],
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
      {
        "C": "CN",
        "ST": "BeiJing",
        "L": "BeiJing",
        "O": "system:kube-scheduler",
        "OU": "4Paradigm"
      }
    ]
}
EOF

```

```

cfssl gencert -ca=/etc/kubernetes/cert/ca.pem \
  -ca-key=/etc/kubernetes/cert/ca-key.pem \
  -config=/etc/kubernetes/cert/ca-config.json \
  -profile=kubernetes kube-scheduler-csr.json | cfssljson -bare kube-scheduler

```

#### 创建和分发kubeconfig文件

```

kubectl config set-cluster kubernetes \
  --certificate-authority=/etc/kubernetes/cert/ca.pem \
  --embed-certs=true \
  --server=https://10.0.94.164:6443 \
  --kubeconfig=kube-scheduler.kubeconfig

```

```

kubectl config set-credentials system:kube-scheduler \
  --client-certificate=kube-scheduler.pem \
  --client-key=kube-scheduler-key.pem \
  --embed-certs=true \
  --kubeconfig=kube-scheduler.kubeconfig

```

```

kubectl config set-context system:kube-scheduler \
  --cluster=kubernetes \
  --user=system:kube-scheduler \
  --kubeconfig=kube-scheduler.kubeconfig

kubectl config use-context system:kube-scheduler --kubeconfig=kube-scheduler.kubeconfig

```

`cp kube-scheduler.kubeconfig /etc/kubernetes`

#### 创建和分发kube-scheduler system unit 文件

```

cat > kube-scheduler.service <<EOF
[Unit]
Description=Kubernetes Scheduler
Documentation=https://github.com/GoogleCloudPlatform/kubernetes

[Service]
ExecStart=/opt/k8s/bin/kube-scheduler \\
  --address=127.0.0.1 \\
  --kubeconfig=/etc/kubernetes/kube-scheduler.kubeconfig \\
  --alsologtostderr=true \\
  --logtostderr=false \\
  --log-dir=/var/log/kubernetes \\
  --v=2
Restart=on-failure
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
EOF

```

`cp kube-scheduler.service /etc/systemd/system`

#### 启动kube-scheduler

`systemctl daemon-reload && systemctl enable kube-scheduler && systemctl restart kube-scheduler`


#### 查看输出metric

`netstat -lnpt|grep 10251`

`curl -s http://127.0.0.1:10251/metrics |head`