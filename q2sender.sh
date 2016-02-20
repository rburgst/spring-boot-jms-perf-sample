#!/bin/bash
SPRING_PROFILES_ACTIVE=sender JMS_QUEUENAME=queue2 gradle bootRun
