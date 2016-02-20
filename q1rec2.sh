#!/bin/bash
SPRING_PROFILES_ACTIVE=receiver JMS_SELECTOR=CLIENT_ID=2 gradle bootRun
