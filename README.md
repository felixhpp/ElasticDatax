

## 构建

#### 通过源码构建
   安装maven, 还需要JDK1.8
   
   `mvn clean package`
   
   相关配置文件会打包到项目目录config文件夹中
   es 数据转换文件在elastic文件夹中

#### 直接下载打包的好的压缩包

### 部署

1.下载压缩包后，解压到相应的目录，
2.修改配置文件
3. windows运行`bin/startup.bat`文件

### 缓存

1. 字典持久化缓存
    
    Ehcache默认配置的话 为了提高效率，所以有一部分缓存是在内存中，然后达到配置的内存对象总量，则才根据策略持久化到硬盘中，这里是有一个问题。假如系统突然中断运行 那内存中的那些缓存，直接被释放掉了，不能持久化到硬盘

    缓存策略：elastic-mapper文件及字典类型数据全部缓存到本地磁盘，具体路径在当前项目ehcahce文件中。
    
    当接受到数据进行转换时，从缓存中读取对应的数据，如果在缓存中找不到相应的数据，则从数据库查询，然后存入到缓存中。此外，在系统第一次启动时
    会初始化对相关文件及字典数据预先存储到缓存中。
    
2. 其他数据可根据情况灵活定制其他缓存策略
    动态SQL查询类型缓存
   
 #### jar包安装到本地maven仓库
   mvn install:install-file -Dfile="D:\CacheDB.jar" -DgroupId=com.cache -DartifactId=cache -Dversion=1.0.0 -Dpackaging=jar