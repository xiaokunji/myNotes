[toc]

# 1. 简介

Airflow是一个可编程，调度和监控的工作流平台，基于有向无环图(DAG)，airflow可以定义一组有依赖的任务，按照依赖依次执行。airflow提供了丰富的命令行工具用于系统管控，而其web管理界面同样也可以方便的管控调度任务，并且对任务运行状态进行实时监控，方便了系统的运维和管理。

Airflow 中常见的名词概念：

- **DAG**

  DAG 意为有向无循环图，在 Airflow 中则定义了整个完整的作业。同一个 DAG 中的所有 Task 拥有相同的调度时间。

- **Task**

  Task 为 DAG 中具体的作业任务，它必须存在于某一个 DAG 之中。Task 在 DAG 中配置依赖关系，跨 DAG 的依赖是可行的，但是并不推荐。跨 DAG 依赖会导致 DAG 图的直观性降低，并给依赖管理带来麻烦。

- **DAG Run**

  当一个 DAG 满足它的调度时间，或者被外部触发时，就会产生一个 DAG Run。可以理解为由 DAG 实例化的实例。

- **Task Instance**

  当一个 Task 被调度启动时，就会产生一个 Task Instance。可以理解为由 Task 实例化的实例。



# 2、Airflow 的服务构成

一个正常运行的 Airflow 系统一般由以下几个服务构成

- **WebServer**

  ​		Airflow 提供了一个可视化的 Web 界面。启动 WebServer 后，就可以在 Web 界面上查看定义好的 DAG 并监控及改变运行状况。也可以在 Web 界面中对一些变量进行配置。

- **Worker**

  ​		一般来说我们用 Celery Worker 来执行具体的作业。Worker 可以部署在多台机器上，并可以分别设置接收的队列。当接收的队列中有作业任务时，Worker 就会接收这个作业任务，并开始执行。Airflow 会自动在每个部署 Worker 的机器上同时部署一个 Serve Logs 服务，这样我们就可以在 Web 界面上方便的浏览分散在不同机器上的作业日志了。

- **Scheduler**

  ​		整个 Airflow 的调度由 Scheduler 负责发起，每隔一段时间 Scheduler 就会检查所有定义完成的 DAG 和定义在其中的作业，如果有符合运行条件的作业，Scheduler 就会发起相应的作业任务以供 Worker 接收。

- **Flower**

  ​		Flower 提供了一个可视化界面以监控所有 Celery Worker 的运行状况。这个服务并不是必要的。

![img](https://pic4.zhimg.com/80/v2-35a160b63e7389fe12f451e299ab0c00_720w.jpg)



# 3. Airflow 的 Web 界面

## 1、DAG 列表

![img](https://upload-images.jianshu.io/upload_images/9094111-c4d1bb23df2557e3.png?imageMogr2/auto-orient/strip|imageView2/2/w/1169/format/webp)

DAG 列表

1. 左侧 On/Off 按钮控制 DAG 的运行状态，Off 为暂停状态，On 为运行状态。注意：所有 DAG 脚本初次部署完成时均为 Off 状态。
2. 若 DAG 名称处于不可点击状态，可能为 DAG 被删除或未载入。若 DAG 未载入，可点击右侧刷新按钮进行刷新。注意：由于可以部署若干 WebServer，所以单次刷新可能无法刷新所有 WebServer 缓存，可以尝试多次刷新。
3. Recent Tasks 会显示最近一次 DAG Run（可以理解为 DAG 的执行记录）中 Task Instances（可以理解为作业的执行记录）的运行状态，如果 DAG Run 的状态为 running，此时显示最近完成的一次以及正在运行的 DAG Run 中所有 Task Instances 的状态。
4. Last Run 显示最近一次的 execution date。注意：execution date 并不是真实执行时间，具体细节在下文 DAG 配置中详述。将鼠标移至 execution date 右侧 info 标记上，会显示 start date，start date 为真实运行时间。start date 一般为 execution date 所对应的下次执行时间。

## 2、作业操作框

在 DAG 的树状图和 DAG 图中都可以点击对应的 Task Instance 以弹出 Task Instance 模态框，以进行 Task Instance 的相关操作。注意：选择的 Task Instance 为对应 DAG Run 中的 Task Instance。

![img](https://upload-images.jianshu.io/upload_images/9094111-b857d859b23a2650.png?imageMogr2/auto-orient/strip|imageView2/2/w/649/format/webp)

作业操作框

1. 在作业名字的右边有一个漏斗符号，点击后整个 DAG 的界面将只显示该作业及该作业的依赖作业。当该作业所处的 DAG 较大时，此功能有较大的帮助。
2. Task Instance Details 显示该 Task Instance 的详情，可以从中得知该 Task Instance 的当前状态，以及处于当前状态的原因。例如，若该 Task Instance 为 no status 状态，迟迟不进入 queued 及 running 状态，此时就可通过 Task Instance Details 中的 Dependency 及 Reason 得知原因。
3. Rendered 显示该 Task Instance 被渲染后的命令。
4. Run 指令可以直接执行当前作业。
5. Clear 指令为清除当前 Task Instance 状态，**清除任意一个 Task Instance 都会使当前 DAG Run 的状态变更为 running**。注意：如果被清除的 Task Instance 的状态为 running，则会尝试 kill 该 Task Instance 所执行指令，并进入 shutdown 状态，并在 kill 完成后将此次执行标记为 failed（如果 retry 次数没有用完，将标记为 up_for_retry）。Clear 有额外的5个选项，均为多选，这些选项从左到右依次为：
   - **Past**: 同时清除所有过去的 DAG Run 中此 Task Instance 所对应的 Task Instance。
   - **Future**: 同时清除所有未来的 DAG Run 中此 Task Instance 所对应的 Task Instance。注意：仅清除已生成的 DAG Run 中的 Task Instance。
   - **Upstream**: 同时清除该 DAG Run 中所有此 Task Instance 上游的 Task Instance。
   - **Downstream**: 同时清除该 DAG Run 中所有此 Task Instance 下游的 Task Instance。
   - **Recursive**: 当此 Task Instance 为 sub DAG 时，循环清除所有该 sub DAG 中的 Task Instance。注意：若当此 Task Instance 不是 sub DAG 则忽略此选项。
6. Mark Success 指令为讲当前 Task Instance 状态标记为 success。注意：如果该 Task Instance 的状态为 running，则会尝试 kill 该 Task Instance 所执行指令，并进入 shutdown 状态，并在 kill 完成后将此次执行标记为 failed（如果 retry 次数没有用完，将标记为 up_for_retry）。



# 4. DAG 配置

Airflow 中的 DAG 是由 Python 脚本来配置的，因而可扩展性非常强。Airflow 提供了一些 DAG 例子，

```python
# -*- coding: utf-8 -*-

import airflow
from airflow.operators.bash_operator import BashOperator
from airflow.operators.dummy_operator import DummyOperator
from airflow.models import DAG


args = {
    'owner': 'airflow',
    'start_date': airflow.utils.dates.days_ago(2) # 作业的开始时间，即作业将在这个时间点以后开始调度。
}

dag = DAG(
    dag_id='example_bash_operator',  # 给 DAG 取一个名字,不能重复
    default_args=args,
    schedule_interval='0 0 * * *'  # 配置 DAG 的执行周期，语法和 crontab 的一致
)

cmd = 'ls -l'
run_this_last = DummyOperator(task_id='run_this_last', dag=dag)

run_this = BashOperator(
    task_id='run_after_loop', bash_command='echo 1', dag=dag)
run_this.set_downstream(run_this_last)

for i in range(3):
    i = str(i)
    task = BashOperator(
        task_id='runme_'+i,
        bash_command='echo "{{ task_instance_key_str }}" && sleep 1',
        dag=dag)
    task.set_downstream(run_this)

task = BashOperator(
    task_id='also_run_this',
    bash_command='echo "run_id={{ run_id }} | dag_run={{ dag_run }}"',
    dag=dag)

# 用[]可以并行执行多个job, task >> [job1,job2]
# 等同于 task.set_downstream(run_this_last)
task >> run_this_last


```



> 那么现在，让我们看一下当一个新配置的 DAG 生效后第一次调度会在什么时候。其实第一次调度时间是在作业中配置的 start date 的第二个满足 schedule interval 的时间点，并且记录的 execution date 为作业中配置的 start date 的第一个满足 schedule interval 的时间点.
>
> 假设我们配置了一个作业的 start date 为 **2017年10月1日**，配置的 schedule interval 为 **00 12 * * *** 那么第一次执行的时间将是 **2017年10月2日 12点** 而此时记录的 execution date 为 **2017年10月1日 12点**。因此 execution date 并不是如其字面说的表示执行时间，真正的执行时间是 execution date 所显示的时间的下一个满足 schedule interval 的时间点。



> [浅谈](https://www.jianshu.com/p/e878bbc9ead2)
>
> [实战](https://zhuanlan.zhihu.com/p/43383509)
>
> [使用](https://www.cnblogs.com/cord/p/9450910.html)
>
> [搭建及问题](https://zhuanlan.zhihu.com/p/36043468)
>
> [官网](https://airflow.apache.org/docs/stable/project.html)







