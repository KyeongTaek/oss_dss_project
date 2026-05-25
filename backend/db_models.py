from tortoise import fields
from tortoise.models import Model


class DbConnectionCheck(Model):
    id = fields.IntField(pk=True)

    class Meta:
        table = "_db_connection_check_dummy"
