import sys, csv

class Node:
    def __init__(self, raw_body):
        self.type = 'Assignment'

        (node_num, raw_domains) = raw_body.split(",", 1)

        self.node_num = int(node_num)

        raw_domains = raw_domains.strip('<').strip('>').strip()
        self.domains = dict()
        domain_counter = 0

        while len(raw_domains) > 0:
            if raw_domains[0] == '{':
                # Unassigned domain
                (parsed_domain, raw_domains) = raw_domains[1:].split('}', 1)
                self.domains[domain_counter] = [int(num) for num in parsed_domain.split(',')]
            elif raw_domains[0].isdigit() or raw_domains[0] == '-':
                # Assigned domain
                if raw_domains.find(',') != -1: # Still domains needing to be split
                    (num, raw_domains) = raw_domains.split(',', 1)
                else:
                    # End of the domains
                    num = raw_domains
                    raw_domains = ''

                self.domains[domain_counter] = int(num)

            domain_counter += 1
            # Remove the trailing comma
            if len(raw_domains) > 0 and raw_domains[0] == ',':
                raw_domains = raw_domains[1:]


class Solution:
    def __init__(self, solution_counter, body):
        self.type = 'Solution'
        self.node_num = 'Solution_' + str(solution_counter)

        self.domains = [int(num) for num in body.split(" ")]
        pass


class DeadEnd:
    def __init__(self, dead_end_counter):
        self.type = 'DeadEnd'
        self.node_num = 'Dead_End_' + str(dead_end_counter)
        self.domains = None


class Tree:
    def __init__(self, dump_tree_file):
        raw_lines = open(dump_tree_file, 'r').readlines()
        tags = ["Node", "SearchAssign", "SearchAction", "Sol", "Problem solvable?"]

        split_lines = []
        for line in raw_lines:
            parts = line.split(":", 1)  # split once

            if parts[0] in tags:
                split_lines.append((parts[0], parts[1].strip()))

        # Check that a solution exist
        for split_line in split_lines:
            if split_line[0] == "Problem solvable?":
                if split_line[1] == "yes":
                    self.solvable = True
                else:
                    assert split_line[1] == "no"
                    self.solvable = False

        assert hasattr(self, "solvable")

        # Merge adjacent solution lines into one
        i = 0
        while i < len(split_lines) - 1:
            (tag, rest) = split_lines[i]
            (tag2, rest2) = split_lines[i+1]
            if tag != "Sol" or tag2 != "Sol":
                i += 1
            else:
                new_string = rest.strip() + " " + rest2.strip()
                split_lines[i:i+2] = [("Sol", new_string)]

        self.root = False
        current_node = False
        current_assign = False

        dead_end_counter = 1
        solution_counter = 1

        num_lines = len(split_lines)

        for i in range(num_lines):
            percent = 100.00 * (i / float(num_lines))
            sys.stdout.write("\r%d%% parsed" % percent)
            sys.stdout.flush()

            (tag, body) = split_lines[i]

            if not self.root and tag == 'Node':
                # first node where no assignments have happened yet
                self.root = Node(body)
                current_node = self.root
            else:
                if tag == 'Node':
                    # handles node 0 (before any assignments have been made)
                    if current_assign == '=' or (hasattr(current_node, 'node_num') and current_node.node_num == 0):
                        current_node.left = Node(body)
                        current_node.left.parent = current_node
                        current_node = current_node.left
                    elif current_assign == '!=':
                        current_node.right = Node(body)
                        current_node.right.parent = current_node
                        current_node = current_node.right

                    current_assign = False
                elif tag == 'SearchAssign' and not current_assign:
                    (part_1, assignment, part_2) = body.split(' ')
                    current_assign = assignment

                    if assignment == '=':
                        current_node.assignment_pair = (part_1, part_2)
                    elif assignment == '!=':
                        if current_node.type == 'Assignment':
                            new_dead_end = DeadEnd(dead_end_counter)

                            if not hasattr(current_node, 'left'):
                                current_node.left = new_dead_end
                                current_node.left.parent = current_node
                                current_node.left.assignment_pair = (part_1, part_2)
                            else:
                                current_node.right = new_dead_end
                                current_node.right.parent = current_node
                                current_node.right.assignment_pair = (part_1, part_2)

                            dead_end_counter += 1

                        # backtrack to the last valid assignment before the current invalid assignment
                        while current_node.type == 'Solution' or current_node.assignment_pair != (part_1, part_2):
                            current_node.back_track = True
                            current_node = current_node.parent
                elif tag == 'SearchAction' and body == 'bt':
                    # forget the last assignment that was tried
                    current_assign = False
                elif tag == 'Sol':
                    sol = Solution(solution_counter, body)
                    solution_counter += 1
                    if current_assign in ['=', False]:
                        current_node.left = sol
                        current_node.left.parent = current_node
                        current_node = current_node.left
                    elif current_assign == '!=':
                        current_node.right = sol
                        current_node.right.parent = current_node
                        current_node = current_node.right

                    current_assign = False

        print("\r100% parsed")

    def export_tree(self, neo4j_path):
        print("Exporting tree...")

        nodes_file = open(neo4j_path + "_nodes.csv", 'w')
        nodes_writer = csv.writer(nodes_file, lineterminator='\n')
        nodes_writer.writerow(['node_num:ID', 'domains', ':LABEL'])

        relationships_file = open(neo4j_path + "_relationships.csv", 'w')
        relationships_writer = csv.writer(relationships_file, lineterminator='\n')
        relationships_writer.writerow([':TYPE', ':START_ID', 'action', ':END_ID'])

        self._export_tree(self.root, nodes_writer, relationships_writer)

        nodes_file.close()
        relationships_file.close()

    def _export_tree(self, current, nodes_writer, relationships_writer):
        nodes_writer.writerow((current.node_num, current.domains, current.type))

        relationship_row = [None] * 4
        relationship_row[1] = current.node_num

        if hasattr(current, 'left'):
            relationship_row[0] = 'EQUALS'
            relationship_row[3] = current.left.node_num

            if hasattr(current, 'assignment_pair'):
                (part1, part2) = current.assignment_pair
                relationship_row[2] = part1 + ' = ' + part2

            relationships_writer.writerow(relationship_row)

        if hasattr(current, 'right'):
            relationship_row[0] = 'NOT_EQUALS'
            relationship_row[2] = None
            relationship_row[3] = current.right.node_num

            if hasattr(current, 'assignment_pair'):
                (part1, part2) = current.assignment_pair
                relationship_row[2] = part1 + ' != ' + part2

            relationships_writer.writerow(relationship_row)

        if hasattr(current, 'back_track') or current.type is 'DeadEnd':
            relationship_row[0] = 'BACKTRACK'
            relationship_row[2] = None
            relationship_row[3] = current.parent.node_num

            relationships_writer.writerow(relationship_row)

        if hasattr(current, 'left'):
            self._export_tree(current.left, nodes_writer, relationships_writer)

        if hasattr(current, 'right'):
            self._export_tree(current.right, nodes_writer, relationships_writer)

"""
Invoke script with:
parsey.py {path_to_dump_tree_text_file} {directory_to_export_csvs_to}
"""
dump_tree_path = sys.argv[1]
output_path = sys.argv[2]

dump_tree = dump_tree_path.split("\\")[-1]
problem = dump_tree.split(".txt")[0]

t = Tree(dump_tree_path)
t.export_tree(output_path + "\\" + problem)