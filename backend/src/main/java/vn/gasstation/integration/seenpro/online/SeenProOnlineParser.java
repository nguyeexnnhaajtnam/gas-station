package vn.gasstation.integration.seenpro.online;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Parses SeenPro's ordered getValue*(standMAC, maCot, nguoiQL, maNL, macmaster, macslave, idConnect, idPump, trangThaix) contract. */
@Component
public class SeenProOnlineParser {
    private static final Logger log = LoggerFactory.getLogger(SeenProOnlineParser.class);
    private static final Pattern BOOTSTRAP_INVOCATION =
        Pattern.compile("\\bgetValue[\\p{Alnum}_$]*\\s*\\(");
    private static final Pattern BOOTSTRAP_FUNCTION_DECLARATION =
        Pattern.compile("\\bfunction\\s+getValue[\\p{Alnum}_$]*\\s*\\([^)]*\\)\\s*\\{");
    private static final Pattern PUMP_DISPLAY_NAME = Pattern.compile("(?iu)\\bCột\\s*\\d+\\b");
    private static final int REQUIRED_ARGUMENT_COUNT = 9;

    public List<SeenProPumpDescriptor> parse(String html) {
        var sourceResults = new ArrayList<SourceParseResult>();
        var failures = new ArrayList<SeenProOnlineBootstrapIncompleteException>();
        var document = Jsoup.parse(html == null ? "" : html);
        document.select("script:not([src])")
            .forEach(script -> sourceResults.add(parseSource(script.data(), failures)));

        // SeenPro keeps its reusable getValue* implementation in a large runtime script and
        // emits a separate bootstrap script for every physical pump. Runtime code can contain
        // concrete invocations too, but those are not pump declarations. When standalone
        // bootstrap scripts exist, they are the canonical source of descriptors.
        var standaloneDescriptors = sourceResults.stream()
            .filter(result -> !result.definesBootstrapFunction())
            .flatMap(result -> result.descriptors().stream())
            .toList();
        var descriptors = standaloneDescriptors.isEmpty()
            ? sourceResults.stream().flatMap(result -> result.descriptors().stream()).toList()
            : standaloneDescriptors;

        if (descriptors.isEmpty()) {
            if (!failures.isEmpty()) throw failures.get(0);
            throw new SeenProOnlineBootstrapIncompleteException(List.of("getValueInvocation"));
        }
        log.debug("[SEENPRO] event=parser.bootstrap.sources provider=seenpro parser=online-bootstrap sourceCount={} standaloneSourceCount={} descriptorCount={}",
            sourceResults.size(), sourceResults.stream().filter(result -> !result.definesBootstrapFunction()
                && !result.descriptors().isEmpty()).count(), descriptors.size());
        return descriptors.stream().map(descriptor -> withDisplayName(document, descriptor)).toList();
    }

    private static SourceParseResult parseSource(String source,
                                                 List<SeenProOnlineBootstrapIncompleteException> failures) {
        if (source == null || source.isEmpty()) return new SourceParseResult(false, List.of());
        var descriptors = new ArrayList<SeenProPumpDescriptor>();
        boolean definesBootstrapFunction = BOOTSTRAP_FUNCTION_DECLARATION.matcher(source).find();
        int cursor = 0;
        var matcher = BOOTSTRAP_INVOCATION.matcher(source);
        var declarationRanges = functionDeclarationRanges(source);
        while (matcher.find(cursor)) {
            int name = matcher.start();
            cursor = matcher.end();
            if (isFunctionDeclaration(source, name) || inside(name, declarationRanges)) continue;
            try {
                ParsedInvocation invocation = invocation(source, matcher.end() - 1);
                cursor = invocation.nextIndex();
                SeenProPumpDescriptor candidate = descriptor(invocation.arguments());
                if (descriptors.contains(candidate)) {
                    log.debug("[SEENPRO] event=parser.bootstrap.recurring-invocation-ignored provider=seenpro parser=online-bootstrap pumpCode={}",
                        diagnostic(candidate.pumpCode()));
                    continue;
                }
                descriptors.add(candidate);
            } catch (SeenProOnlineBootstrapIncompleteException failure) {
                failures.add(failure);
            }
        }
        return new SourceParseResult(definesBootstrapFunction, List.copyOf(descriptors));
    }

    private static ParsedInvocation invocation(String source, int open) {
        var arguments = new ArrayList<String>();
        var current = new StringBuilder();
        char quote = 0;
        boolean escaped = false;
        int nested = 0;
        for (int index = open + 1; index < source.length(); index++) {
            char character = source.charAt(index);
            if (escaped) {
                current.append(unescape(character));
                escaped = false;
                continue;
            }
            if (character == '\\') {
                escaped = true;
                continue;
            }
            if (quote != 0) {
                if (character == quote) quote = 0;
                else current.append(character);
                continue;
            }
            if (character == '\'' || character == '"') {
                quote = character;
            } else if (character == '(' || character == '[' || character == '{') {
                nested++;
                current.append(character);
            } else if (character == ')' && nested == 0) {
                arguments.add(current.toString().trim());
                return new ParsedInvocation(List.copyOf(arguments), index + 1);
            } else if ((character == ')' || character == ']' || character == '}') && nested > 0) {
                nested--;
                current.append(character);
            } else if (character == ',' && nested == 0) {
                arguments.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        throw new SeenProOnlineBootstrapIncompleteException(List.of("unterminatedGetValueInvocation"));
    }

    private static SeenProPumpDescriptor descriptor(List<String> arguments) {
        log.debug("[SEENPRO] event=parser.bootstrap.arguments provider=seenpro parser=online-bootstrap {}",
            positionalArguments(arguments));
        if (arguments.size() < REQUIRED_ARGUMENT_COUNT) {
            throw new SeenProOnlineBootstrapIncompleteException(
                List.of("getValueRequiredArguments(minimum=" + REQUIRED_ARGUMENT_COUNT
                    + ",actual=" + arguments.size() + ")"));
        }
        for (int index = 0; index < REQUIRED_ARGUMENT_COUNT; index++) {
            requiredLiteral(arguments.get(index), index);
        }
        String standardizedMAC = arguments.get(0);
        String pumpCode = arguments.get(1);
        String user = arguments.get(2);
        String fuelId = arguments.get(3);
        String master = arguments.get(4);
        String slave = arguments.get(5);
        String connectElementId = arguments.get(6);
        String pumpElementId = arguments.get(7);
        String initialPumpState = arguments.get(8);
        var descriptor = new SeenProPumpDescriptor(pumpCode, pumpCode, standardizedMAC, master, slave,
            fuelId, user, connectElementId, pumpElementId, initialPumpState);
        return descriptor;
    }

    private static SeenProPumpDescriptor withDisplayName(Document document,
                                                           SeenProPumpDescriptor descriptor) {
        String displayName = findDisplayName(document, descriptor.pumpElementId());
        if (displayName == null) displayName = findDisplayName(document, descriptor.connectElementId());
        SeenProPumpDescriptor resolved = displayName == null ? descriptor
            : new SeenProPumpDescriptor(descriptor.pumpCode(), displayName, descriptor.standardizedMAC(),
                descriptor.master(), descriptor.slave(), descriptor.fuelId(), descriptor.user(),
                descriptor.connectElementId(), descriptor.pumpElementId(), descriptor.initialPumpState());
        logMapped(resolved);
        return resolved;
    }

    private static void logMapped(SeenProPumpDescriptor descriptor) {
        log.debug("[SEENPRO] event=parser.bootstrap.mapped provider=seenpro parser=online-bootstrap pumpCode={} pumpName={} standardizedMAC={} master={} slave={} fuelId={} user={} connectElementId={} pumpElementId={} initialPumpState={}",
            diagnostic(descriptor.pumpCode()), diagnostic(descriptor.pumpName()),
            diagnostic(descriptor.standardizedMAC()), diagnostic(descriptor.master()),
            diagnostic(descriptor.slave()), diagnostic(descriptor.fuelId()), diagnostic(descriptor.user()),
            diagnostic(descriptor.connectElementId()), diagnostic(descriptor.pumpElementId()),
            diagnostic(descriptor.initialPumpState()));
    }

    private static String findDisplayName(Document document, String elementId) {
        if (elementId == null || elementId.isBlank()) return null;
        Element current = document.getElementById(elementId);
        for (int level = 0; current != null && level < 5; level++, current = current.parent()) {
            var matcher = PUMP_DISPLAY_NAME.matcher(current.text());
            if (matcher.find()) return matcher.group().replaceAll("\\s+", " ").trim();
        }
        return null;
    }

    private static String positionalArguments(List<String> arguments) {
        var result = new StringBuilder("argumentCount=").append(arguments.size());
        for (int index = 0; index < arguments.size(); index++) {
            result.append(" arg").append(index).append('=').append(diagnostic(arguments.get(index)));
        }
        return result.toString();
    }

    private static String diagnostic(String value) {
        if (value == null) return "<null>";
        return '"' + value.replace("\\", "\\\\")
            .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t")
            .replace("\"", "\\\"") + '"';
    }

    private static String requiredLiteral(String raw, int index) {
        String value = raw.trim();
        if (value.isEmpty()) {
            throw new SeenProOnlineBootstrapIncompleteException(List.of("getValueArgument[" + index + "]"));
        }
        return value;
    }

    private static boolean isFunctionDeclaration(String source, int name) {
        String prefix = source.substring(Math.max(0, name - 16), name).trim();
        return prefix.endsWith("function") || prefix.endsWith("function*");
    }

    private static List<SourceRange> functionDeclarationRanges(String source) {
        var ranges = new ArrayList<SourceRange>();
        var matcher = BOOTSTRAP_FUNCTION_DECLARATION.matcher(source);
        while (matcher.find()) {
            int openBrace = matcher.end() - 1;
            int closeBrace = matchingBrace(source, openBrace);
            ranges.add(new SourceRange(matcher.start(), closeBrace < 0 ? source.length() : closeBrace + 1));
        }
        return ranges;
    }

    private static int matchingBrace(String source, int openBrace) {
        int depth = 0;
        char quote = 0;
        boolean escaped = false;
        for (int index = openBrace; index < source.length(); index++) {
            char character = source.charAt(index);
            if (escaped) { escaped = false; continue; }
            if (character == '\\') { escaped = true; continue; }
            if (quote != 0) {
                if (character == quote) quote = 0;
                continue;
            }
            if (character == '\'' || character == '"' || character == '`') { quote = character; continue; }
            if (character == '{') depth++;
            else if (character == '}' && --depth == 0) return index;
        }
        return -1;
    }

    private static boolean inside(int index, List<SourceRange> ranges) {
        return ranges.stream().anyMatch(range -> index >= range.start() && index < range.end());
    }

    private static char unescape(char value) {
        return switch (value) {
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> value;
        };
    }

    private record ParsedInvocation(List<String> arguments, int nextIndex) {}
    private record SourceRange(int start, int end) {}
    private record SourceParseResult(boolean definesBootstrapFunction,
                                     List<SeenProPumpDescriptor> descriptors) {}
}
