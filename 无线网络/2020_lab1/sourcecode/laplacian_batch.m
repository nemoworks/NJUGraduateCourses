%network generation
n=500; %number of nodes
x_size=10; %the width of the rectangular
y_size=10; %the length of the rectangular
range=1;   %communication range
anchor_num=5; % number of anchor nodes
batch=100; % number of batches
iterations=5; % number of iterations


ale=zeros(batch,1);
rt=zeros(batch,1);

%laplacian process
i = 0;
while(i<batch) 
    % generate random (x,y) locations for sensor nodes
    true_loc=generate_random_network(n,x_size,y_size);

    % generate edgelist and node adjacency matrix
    % the first two columns in edge list is the tail and head vertex id
    % the thrid column is the distance of the edge
    % adjacency matrix is a n*n sparse matrix
    [edgelist,adjmatrix]=getedges(true_loc,range);

    %get anchor nodes, there are anchor_num anchor nodes on each side of the
    %rectangular, but actual number of anchors may change.
    %the first column in anchor is the node id of the anchor, the next two
    %columns are x and y coordinates
    anchor=getanchor(true_loc,anchor_num);
    anchor_size=size(anchor,1); % actual number of anchors in the anchor set

    edgeweight=ones(size(edgelist,1),1); % initialize edge weights to 1
    tic
    for loops= 1:iterations %iteratively sove the balanced srping network problem
        %You should write your own program to balance the spring network using
        %graph laplacian
        est_loc= balancenet(n,[edgelist(:,1:2) edgeweight],anchor);
        if(isnan(est_loc))
            fprintf('RCOND(Laplacian) too small to calculate\n');
            break
        end
        %You should write your own program to adjust the weight (stiffness) of
        %edges
        edgeweight=adjustweight(est_loc,[edgelist(:,1:2) edgeweight],range);
    end
    if(isnan(est_loc))
        continue
    end
    running_time = toc;
    errors = (true_loc-est_loc).';
    average_loc_error = sum(sqrt(sum(errors.*errors)))/(n-size(anchor,1));
    fprintf('Laplacian cost time: %f secends\n',running_time);
    fprintf('Laplacian average error: %f\n',average_loc_error);
    if isnan(average_loc_error)
        continue;
    end
    i=i+1;
    rt(i)=running_time;
    ale(i)=average_loc_error;
end

art = sum(rt)/batch;

aale = sum(ale)/batch;

fprintf('Average location error is: %f\n',aale);
fprintf('Average running time is: %f\n',art);

x = sort(ale.');
mu = mean(x);
sigma = std(x);
y = cdf('Normal',x,mu,sigma);

plot(x,y);
