**原因**: 使用modleresource时 返回注入的值是模型,当你放回的值不是模型对应的字段则会报错,比如进行了聚合操作

**解决**: 重写resource,自定义返回的值

 

**SaleResource**

```python
class SaleResource(Resource):

avgPrice = fields.IntegerField(attribute='avgPrice')

maxPrice = fields.DecimalField(attribute='maxPrice')

minPrice = fields.DecimalField(attribute='minPrice')

class Meta:

resource_name = 'sale'

authorization = Authorization()

filtering = {

'id': ALL_WITH_RELATIONS,

'pub_date': ['exact', 'lt', 'lte', 'gte', 'gt'],

}

def get_object_list(self, request):

    reID = request.GET['id']

    logger.debug("需要查询三种价格的ID:" + reID)

    results = []

    queryset = Sale.objects.filter(id=reID) \

    .annotate(avgPrice=Avg("sellPrice"), maxPrice=Max("sellPrice"), minPrice=Min("sellPrice")) \

    .values('avgPrice', 'maxPrice', 'minPrice')

    for item in queryset:

    \# logging.warn(item)

    new_obj = BaseJsonModel()

    new_obj.minPrice = item['minPrice']

    new_obj.maxPrice = item['maxPrice']

    new_obj.avgPrice = item['avgPrice']

    results.append(new_obj)

    return results

def obj_get_list(self, bundle, **kwargs):

	return self.get_object_list(bundle.request)
```



**BaseJsonModel**

```python
class BaseJsonModel(object):

def __init__(self, initial=None):

    self.__dict__['_data'] = {}

    if hasattr(initial, 'items'):

    self.__dict__['_data'] = initial

def __getattr__(self, name):

	return self._data.get(name, None)

def __setattr__(self, name, value):

    self.__dict__['_data'][name] = value

    def to_dict(self):

    return self._data

 
```

