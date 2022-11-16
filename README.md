### Cluster com RabbitMq

```bash
docker run -d --rm --net alura --hostname rabbit1 --name rabbit1 -p 8085:15672 -e RABBITMQ_ERLANG_COOKIE=alura_secret rabbitmq:3.10-management
```

```bash
docker run -d --rm --net alura --hostname rabbit2 --name rabbit2 -p 8086:15672 -e RABBITMQ_ERLANG_COOKIE=alura_secret rabbitmq:3.10-management
```

```bash
docker run -d --rm --net alura --hostname rabbit3 --name rabbit3 -p 8087:15672 -e RABBITMQ_ERLANG_COOKIE=alura_secret rabbitmq:3.10-management
```

Executar o comando abaixo para o rabbit2 e rabbit3 para adicionar como nó no rabbit1
```bash
docker exec -it rabbit2 rabbitmqctl stop_app
docker exec -it rabbit2 rabbitmqctl reset
docker exec -it rabbit2 rabbitmqctl join_cluster rabbit@rabbit1
docker exec -it rabbit2 rabbitmqctl start_app
```

No endereço http://localhost:8085, conseguimos ver os 3 nós ativos.

Na sessão “Admin” da UI do RabbitMQ, adicionamos uma política de alta disponibilidade usando o pattern .* e aplicando all, ou seja, replicando dados por todos os nós.

Na sequência, criamos filas, mensagens e depois paramos um dos nós, confirmando que as filas e mensagens foram corretamente replicadas, mantendo-as em memória, sem necessidade de persistência, garantindo a alta disponibilidade.