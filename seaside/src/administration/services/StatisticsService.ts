import {map, take} from "rxjs/operators";
import {Observable} from "rxjs";
import {Counter} from "@/administration/model/Counter.type";
import {send} from "@/common/services/GraphQLClient";

const STATISTICS_GQL_QUERY = `#graphql
query Statistics {
    statistics{ name icon description value }
}`

export function get(): Observable<Counter[]> {
    return send<{ statistics: Counter[] }>(STATISTICS_GQL_QUERY).pipe(
        map(data => data.data.statistics),
        take(1),
    );
}
