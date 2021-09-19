[toc]
# 通过命令返回值判断
```
if [  `sed -n '$=' a.txt`  -lt 2 ]  ; then
    echo "a.txt文件的行数大于2"
```


# 判断文件夹
```
if [ ! -d "$old_path$1" ];then
    echo " 路径 $old_path$1 不存在,文件夹 $1 不存在"
    return
```