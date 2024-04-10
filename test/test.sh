#!/usr/bin/env bash

function start_aggregation_server()
{
    java -classpath .::../lib/gson-2.10.1.jar AggregationServer &
}

function start_content_server()
{
    java -classpath .:../lib/gson-2.10.1.jar ContentServer 127.0.0.1:4567 "$1" "$2" &
}

function start_client()
{
    java -classpath .:../lib/gson-2.10.1.jar GETClient 127.0.0.1:4567 "./resources/client_out.json"
}

function start_compare()
{
    java -classpath .:../lib/gson-2.10.1.jar TestCompare
}

function handle_kill_single()
{
    if kill -0 "$aggregation_server_pid" 2> /dev/null
    then
        kill "$aggregation_server_pid"
        echo "kill AggregationServer  $aggregation_server_pid"
    fi

    for pid in "${content_server_pids[@]}"
    do
        if kill -0 "$pid" 2> /dev/null
        then
            kill "$pid"
            echo "kill ContentServer $pid"
        fi
    done

    echo "quit"
    exit 0
}

function main()
{
    # kill, Ctrl + C, Ctrl + \ to terminate the test
    trap "handle_kill_single" SIGINT SIGTERM SIGQUIT

    echo "clean files:"
    make clean
    echo -e "\ncompile:"
    make build
    echo "wait 2s ..."
    sleep 2s

    echo -e "\nstart AggregationServer"
    start_aggregation_server > /dev/null
    aggregation_server_pid=$!
    echo "wait 2s ..."
    sleep 2s
    
    content_server_pids=()
    for i in {1..5}
    do
        echo -e "\nstart ContentServer $i"
        start_content_server "$i" "./resources/weather$i.txt" > /dev/null
        content_server_pids+=("$!")
        echo "wait 2s ..."
        sleep 2s
    done

    echo -e "\nstart GETClient"
    start_client
    echo "stop GETClient"
    echo "Verify the source data pushed by the content server and the data obtained by the client request, and the result is: "
    start_compare

    echo -e "\nstop AggregationServer and ContentServer"
    kill "$aggregation_server_pid"
    for i in "${content_server_pids[@]}"
    do
        kill "$i"
    done

    echo "wait 2s ..."
    sleep 2s

    echo -e "\nresume start AggregationServer"
    start_aggregation_server > /dev/null
    aggregation_server_pid=$!
    echo "wait 2s ..."
    sleep 2s

    echo -e "\nstart GETClient"
    start_client
    echo "stop GETClient"
    echo "Restore backup after verifying aggregation server interruption and restart, and the result is: "
    start_compare

    echo -e "\nwait 30s ..."
    for i in {30..0}
    do
        echo "${i}s"
        sleep 1s
    done

    echo -e "\nstart GETClient"
    start_client
    echo "stop GETClient"
    start_compare

    echo -e "\nstop AggregationServer"
    kill "$aggregation_server_pid"
}

if [ "$0" == "${BASH_SOURCE[*]}" ]
then
    main "$@"
fi