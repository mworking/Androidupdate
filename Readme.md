### 项目介绍
    1. 使用okgo操作网络请求
    2. 使用Rxpermission操作权限请求

### 类名介绍
    1. 所有的网络请求类 	OkGoUpdateHttpUtil
    2. 所有的接口回调类	HttpManager
    3. 主体管理类		UpdateAppManager
    4. 设置更新参数配置类	UpdateAppManager.Builder
    5. 进程开启下载类  	DownloadService
    6. 更新实体类        UpdateAppBean
    7. 应用内部水平进度条 HProgressDialogUtils
    8. 通知栏圆形进度条   CProgressDialogUtils
    9. 框架自带升级弹框   UpdateDialogFragment

### UpdateCallback
    该接口中有默认方法，目的是，可以直接用，但是可以覆写其中的方法
