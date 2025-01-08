Self Assessment Individual Details API
========================
This API allows developers to perform operations related to Individuals/Taxable Entities for Income Tax Self Assessment:

- Retrieve ITSA status for a given NINO for a specified tax year and optionally for future years

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.9.7
- [Service Manager V2](https://github.com/hmrc/sm2)

## Development Setup

Run from the console using: `sbt run` (starts on port 7790 by default)

Start the service manager profile: `sm2 --start MTDFB_SA_DETAILS`

## Run Tests

Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

Note: if you run into `java.lang.OutOfMemoryError` errors, add a `.sbtopts` file to the root of the project with the
following contents:

```
-J-Xmx3G
-J-XX:+UseG1GC
```

## Viewing OAS

To view documentation locally ensure the Self Assessment Individual Details API is running, and run
api-documentation-frontend:

```
./run_local_with_dependencies.sh
```

Then go to http://localhost:9680/api-documentation/docs/openapi/preview and enter the full URL path to the YAML file
with the
appropriate port and version:

```
http://localhost:7790/api/conf/2.0/application.yaml
```

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [Self Assessment Individual Details Documentation](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/self-assessment-individual-details-api)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
