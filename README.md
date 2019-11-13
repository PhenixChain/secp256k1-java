# secp256k1-java

包含以下用例：签名、签名验证、转账交易
> 同步区块（获取转账记录流程）

```
以测试环境(http://121.201.80.40:8888/apidoc)为例，其中(kcoin为开元通宝 wallet为8PHC nzc为牛樟链 kcc为锎矿 tt为票链)

一、获取最新区块高度
    请求 http://121.201.80.40:8888/kcoin/getlatestblock
    返回 {
        "code": 1,
        "info": {
            "Hash": "ba9e37c5670c09f099c4a9175a5497bcd5db9b6c6593402a51cd93bc73abfcd9",
            "Height": "95096", //最新区块高度
            "PreHash": "99b2d4cc2b2f6404f042010ddb732f4cc2ae4cb58bf770f4484c3fac91e872cf"
        }
    }

二、扫描区块(获取区块里面的转账记录)
    初次上线的时候应该记录当前链的最新区块高度，例如当时的区块高度为94075，这次应递增区块高度去扫描直到最新高度
    (备注：没必要从区块高度1开始扫描，已经扫描过的区块也没必要再次去扫描)
    请求 http://121.201.80.40:8888/kcoin/getblocktranslist?num=94076
    ... 94077
    ... 94078
    ...
    请求http://121.201.80.40:8888/kcoin/getblocktranslist?num=95096
    例如 返回
    {
        "code": 1,
        "info": [
            {
                "from_type": 1,
                "from_address": "phenix2CkSyo9K5rrdw7aV4gkEAYNfAFjAi7pEgH", //转出地址
                "to_address": "phenix2G9nhGgH8J5w8E2cF18DGiqxUq9Lf7p468",   //转入地址
                "trans_time": 1536119975, //转账时间
                "number": 1.6, //转账金额
                "tx_id": "8e16f30d9fa75abb79d21b4d2e67b8460946dc3aa64d7e246fb05fe60c8b04fa",
                "block": 94076,
                "status": 0, //交易状态（0为有效）
                "direct": ""
            }
        ]
    }
```