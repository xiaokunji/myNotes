以下是基于ucap(Django,contos7)项目:

安装NIGNX:

安装uwsgi:  `pip install uwsgi`

**#配置NGINX**

配置文件: vim /etc/nginx/conf.d/nginx_mas_ucap.conf

 ```


\# nginx_mas_ucap.conf

\# the upstream component nginx needs to connect to

upstream mas_ucap {

  server unix:///opt/mas_ucap/unix.sock;  # for a file socket

  \#server 192.168.153.128:8080;

}

 

\# configuration of the server

server {

  \# the port your site will be served on

  listen   8000;

  \# the domain name it will serve for, substitute your machine's IP address or FQDN

  server_name 192.168.153.128;

  charset   utf-8;

  \# max upload size

  client_max_body_size 75M;

  \# adjust to taste

  client_header_buffer_size 32k;

  large_client_header_buffers 4 32k;

  access_log /var/log/nginx/mas_ucap_test_access.log;

  error_log  /var/log/nginx/mas_ucap_test_error.log;

  \#proxy_connect_timeout 600;

  \#proxy_read_timeout 600;

  \#proxy_send_timeout 600;

 

  \# your Django project's media files - amend as required

  \#location /media {

  \#  alias /usr/share/nginx/html/mas_ucap/media;

  \#}

 

  \# your Django project's static files - amend as required

  location /static/ {

​    alias /usr/share/nginx/html/mas_ucap/static/;

  }

 

  \# Finally, send all non-media requests to the Django server.

  location / {

​    \#uwsgi_pass 192.168.153.128:8080;

​    uwsgi_pass mas_ucap;

​    \# the uwsgi_params file you installed

​    include   /etc/nginx/uwsgi_params;

​    uwsgi_read_timeout 600s;

  }

}
 ```





```

#配置uwsgi
[uwsgi]
#uwsgi 的配置文件,运行这个文件(位置无要求)( 命令: uwsgi  [--ini ]  uwsgi.ini )
# Django-related settings
# the base directory (full path)
 
chdir           = /home/xkj/MAS/project/mas-ucap-2017_test/web
# Django's wsgi file
module          = sitecore.wsgi
# the virtualenv (full path)
home            = /home/xkj/Envs/ucap
 
# process-related settings
# master
master          = true
# maximum number of worker processes
processes       = 2
# the socket (use the full path to be safe
socket          = /opt/mas_ucap/unix2.sock
# ... with appropriate permissions - may be needed
# chmod-socket    = 664
# clear environment on exit
vacuum          = true
 
# set an environment variable
env = DJANGO_SETTINGS_MODULE=sitecore.settings
# create a pidfile
pidfile = mas_ucap2.pid
# respawn processes taking more than 20 seconds
harakiri = 600
# limit the project to 128 MB
# limit-as = 128
# respawn processes after serving 5000 requests
max-requests = 5000
# background the process & log
daemonize = /opt/mas_ucap/log/uwsgi.log
```

