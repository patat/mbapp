rootProject.name = "mbapp"

include(
    "applications:basic-server",
    "applications:data-analyzer-server",
    "applications:data-collector-server",

    "components:data-collector",
    "components:data-analyzer",
    "components:results-awaiter",
    "components:model",

    "support:workflow-support",
    "support:rabbit-support",

    "databases:mb-db"
)
